package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

public class DynamicClassLoader<CCTX, MCTX, SCTX, TCTX, ICTX> extends TransformingClassLoader
{
	private static final String			RECEIVER_CONTEXT_FIELD_NAME	= "receiverContext";
	private static final Constructor<?>	Object_new;
	private static final Method			StaticMethodHandler_call;
	private static final Method			InstanceMethodHandler_call;
	static
	{
		try
		{
			Object_new = Object.class.getConstructor();
			StaticMethodHandler_call = StaticMethodHandler.class.getMethod("call", Object[].class);
			// second argument is an unbounded type parameter, so its erased type is Object
			InstanceMethodHandler_call = InstanceMethodHandler.class.getMethod("call", Object.class, Object.class, Object[].class);
		} catch(NoSuchMethodException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	private final DynamicInterfaceProvider									interfaceProvider;
	private final DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>	invocationHandler;
	private final boolean													preferOriginalClasses;

	public DynamicClassLoader(DynamicInterfaceProvider interfaceProvider,
			DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler, boolean preferOriginalClasses)
	{
		this.interfaceProvider = interfaceProvider;
		this.invocationHandler = invocationHandler;
		this.preferOriginalClasses = preferOriginalClasses;
	}

	@Override
	protected Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL)
	{
		System.out.println("Loading " + name);
		// Delegate classes referenced by / stored in dynamically-generated classes to parent; don't define them ourself:
		// Otherwise, we get weird ClassCastExceptions.
		//TODO not pretty; feels very hardcoded. Also, this could cause problems if users (Charon-side or called code) use other classes.
		// Maybe discern by using originalClassfileURL? Or package name (don't redefine any Charon classes)?
		//TODO instead of preventing delegating to parent, just load all user classes (called code) through the DynamicClassLoader,
		// or through an even "lower" classloader.
		if(name.equals(StaticMethodHandler.class.getName()) ||
				name.equals(InstanceMethodHandler.class.getName()))
			try
			{
				return getParent().loadClass(name);
			} catch(ClassNotFoundException e)
			{
				return null;
			}

		Class<?> clazz = null;

		if(preferOriginalClasses)
			if(clazz == null)
				clazz = defineClassOrNull(name, originalClassfile);

		if(clazz == null)
			clazz = mockClassOrNull(name);

		if(!preferOriginalClasses)
			if(clazz == null)
				clazz = defineClassOrNull(name, originalClassfile);

		return clazz;
	}

	private Class<?> mockClassOrNull(String name)
	{
		ClassInterface classInterface = interfaceProvider.interfaceForClass(name);
		return classInterface == null ? null : mockClassWithInterface(name, classInterface);
	}

	private Class<?> defineClassOrNull(String name, byte[] bytes) throws ClassFormatError
	{
		return bytes == null ? null : defineClass(name, bytes, 0, bytes.length);
	}

	private Class<?> mockClassWithInterface(String name, ClassInterface classInterface)
	{
		CCTX classContext = invocationHandler.createClassContext(classInterface);

		DynamicType.Builder<?> builder = new ByteBuddy()
				//TODO modify here if we support inheritance
				.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.name(name)
				.defineField(RECEIVER_CONTEXT_FIELD_NAME, Object.class, Visibility.PRIVATE, FieldManifestation.FINAL);

		for(ConstructorInterface constructor : classInterface.constructors())
			builder = defineDynamicConstructor(builder, classContext, constructor);

		for(MethodInterface method : classInterface.methods())
			builder = defineDynamicMethod(builder, classContext, method);

		DynamicType.Unloaded<?> unloaded = builder.make();
		DynamicType.Loaded<?> dynamicType = unloaded.load(this, new ClassLoadingStrategy<>()
		{
			public Map<TypeDescription, Class<?>> load(DynamicClassLoader<CCTX, MCTX, SCTX, TCTX, ICTX> classLoader, Map<TypeDescription, byte[]> types)
			{
				return types
						.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> classLoader.defineClass(name, e.getValue(), 0, e.getValue().length)));
			}
		});
		return dynamicType.getLoaded();
	}

	private DynamicType.Builder<?> defineDynamicConstructor(DynamicType.Builder<?> builder, CCTX classContext, ConstructorInterface constructor)
	{
		TCTX constructorContext = invocationHandler.createConstructorContext(classContext, constructor);
		StaticMethodHandler target = args -> invocationHandler.invokeConstructor(classContext, constructorContext, args);

		return builder
				.defineConstructor()
				.withParameters(constructor.parameterTypes())
				.intercept(MethodCall.invoke(Object_new).andThen(MethodCall
						.invoke(StaticMethodHandler_call)
						.on(target, StaticMethodHandler.class)
						.withArgumentArray()
						.setsField(ElementMatchers.named(RECEIVER_CONTEXT_FIELD_NAME))));
	}

	private ReceiverTypeDefinition<?> defineDynamicMethod(DynamicType.Builder<?> builder, CCTX classContext, MethodInterface method)
	{
		MethodCall implementation;
		if(method.isStatic())
		{
			SCTX methodContext = invocationHandler.createStaticMethodContext(classContext, method);
			StaticMethodHandler target = args -> invocationHandler.invokeStaticMethod(classContext, methodContext, args);
			implementation = MethodCall.invoke(StaticMethodHandler_call).on(target, StaticMethodHandler.class);
		} else
		{
			MCTX methodContext = invocationHandler.createInstanceMethodContext(classContext, method);
			InstanceMethodHandler<ICTX> target = (receiver, receiverContext, args) -> invocationHandler.invokeInstanceMethod(
					classContext, methodContext, receiver, receiverContext, args);
			implementation = MethodCall.invoke(InstanceMethodHandler_call).on(target, InstanceMethodHandler.class).withThis().withField(RECEIVER_CONTEXT_FIELD_NAME);
		}

		return builder
				.defineMethod(method.name(), method.returnType(),
						method.isStatic() ? List.of(Visibility.PUBLIC, Ownership.STATIC) : List.of(Visibility.PUBLIC))
				.withParameters(method.parameters())
				.intercept(implementation
						.withArgumentArray()
						.withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
	}

	/**
	 * Not meant to be visible to users, but has to be public for classloading reasons:
	 * The (classes of the) lambda expressions implementing {@link StaticMethodHandler} are loaded by the parent loader,
	 * and thus their superclass {@link StaticMethodHandler} as well.
	 * The dynamically-generated classes have to contain fields referencing those lambdas, and are obviously loaded by ourself.
	 * Because of this, we can't redefine {@link StaticMethodHandler}, because otherwise the lambdas couldn't be assigned to the fields
	 * as the superclass of the lambdas is the {@link StaticMethodHandler} class defined by the parent
	 * instead of our redefined {@link StaticMethodHandler} class.
	 */
	public static interface StaticMethodHandler
	{
		// When renaming this method or changing its interface, remember to adjust corresponding Method constant
		public Object call(Object[] args);
	}
	/**
	 * See {@link StaticMethodHandler}
	 */
	public static interface InstanceMethodHandler<ICTX>
	{
		// When renaming this method or changing its interface, remember to adjust corresponding Method constant
		public Object call(Object receiver, ICTX receiverContext, Object[] args);
	}
}
