package net.haspamelodica.charon.mockclasses.dynamicclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

// TODO split not redirecting to parent from dynamically defining classes.
// Relatedly, make not redirecting to parent optional.
public class DynamicClassLoader<CCTX, MCTX, SCTX, TCTX, ICTX> extends TransformingClassLoader
{
	private static final String								INSTANCE_CONTEXT_FIELD_NAME				= "instanceContext";
	private static final ElementMatcher<FieldDescription>	INSTANCE_CONTEXT_FIELD_MATCHER			= ElementMatchers.named(INSTANCE_CONTEXT_FIELD_NAME);
	private static final FieldNameExtractor					INSTANCE_CONTEXT_FIELD_NAME_EXTRACTOR	= method -> INSTANCE_CONTEXT_FIELD_NAME;

	public static final Constructor<?>	Object_new;
	private static final Method			StaticMethodHandler_call;
	private static final Method			ConstructorMethodHandler_call;
	private static final Method			InstanceMethodHandler_call;
	static
	{
		try
		{
			Object_new = Object.class.getConstructor();
			StaticMethodHandler_call = StaticMethodHandler.class.getMethod("call", Object[].class);
			ConstructorMethodHandler_call = ConstructorMethodHandler.class.getMethod("call", Object.class, Object[].class);
			// second argument is an unbounded type parameter, so its erased type is Object
			InstanceMethodHandler_call = InstanceMethodHandler.class.getMethod("call", Object.class, Object.class, Object[].class);
		} catch(NoSuchMethodException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	private final Set<String>				forceDelegationClassnames;
	private final DynamicInterfaceProvider	interfaceProvider;
	private final DynamicClassTransformer	transformer;
	private final boolean					preferOriginalClasses;

	private final DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler;

	public DynamicClassLoader(DynamicInterfaceProvider interfaceProvider,
			DynamicClassTransformer transformer, boolean preferOriginalClasses,
			DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler, Class<?>... forceDelegationClasses)
	{
		this(Arrays.stream(forceDelegationClasses).map(Class::getName).collect(Collectors.toUnmodifiableSet()),
				interfaceProvider, transformer, preferOriginalClasses, invocationHandler);
	}
	public DynamicClassLoader(Set<String> forceDelegationClassnames, DynamicInterfaceProvider interfaceProvider,
			DynamicClassTransformer transformer, boolean preferOriginalClasses,
			DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler)
	{
		// Delegate classes referenced by / stored in dynamically-generated classes to parent; don't define them ourself.
		// Otherwise, we get weird ClassCastExceptions.
		//TODO not pretty; feels very hardcoded. Also, this could cause problems if users (Charon-side or called code)
		// reference other classes in dynamically-generated classes.
		// Maybe discern by using originalClassfileURL? Or package name (don't redefine any Charon classes)?
		//TODO instead of preventing delegating to parent, just load all user classes (called code) through the DynamicClassLoader,
		// or through an even "lower" classloader.
		this.forceDelegationClassnames = Stream.concat(Stream.of(
				StaticMethodHandler.class, ConstructorMethodHandler.class, InstanceMethodHandler.class)
				.map(Class::getName),
				forceDelegationClassnames.stream()).collect(Collectors.toUnmodifiableSet());
		this.interfaceProvider = interfaceProvider;
		this.transformer = transformer;
		this.invocationHandler = invocationHandler;
		this.preferOriginalClasses = preferOriginalClasses;
	}

	@Override
	protected Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL)
	{
		if(forceDelegationClassnames.contains(name))
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
		TypeDefinition typeDefinition = interfaceProvider.typeDefinitionFor(name);
		return typeDefinition == null ? null : mockType(name, typeDefinition);
	}

	private Class<?> defineClassOrNull(String name, byte[] bytes) throws ClassFormatError
	{
		return bytes == null ? null : defineClass(name, bytes, 0, bytes.length);
	}

	private Class<?> mockType(String name, TypeDefinition typeDefinition)
	{
		if(typeDefinition.isInterface())
			throw new UnsupportedOperationException("not implemented yet");
		return mockClass(name, typeDefinition);
	}
	private Class<?> mockClass(String name, TypeDefinition typeDefinition)
	{
		CCTX classContext = invocationHandler.createClassContext(typeDefinition);

		DynamicType.Builder<?> builder = new ByteBuddy()
				//TODO modify here if we support inheritance
				.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.implement(typeDefinition.getInterfaces())
				.name(name)
				.defineField(INSTANCE_CONTEXT_FIELD_NAME, Object.class, Visibility.PRIVATE, FieldManifestation.FINAL);

		for(MethodDescription method : typeDefinition.getDeclaredMethods())
			builder = method.isConstructor()
					? defineDynamicConstructor(builder, classContext, method)
					: defineDynamicMethod(builder, classContext, method);

		builder = transformer.transform(builder, INSTANCE_CONTEXT_FIELD_MATCHER, INSTANCE_CONTEXT_FIELD_NAME_EXTRACTOR);

		DynamicType.Loaded<?> dynamicType = builder.make().load(this, new ClassLoadingStrategy<>()
		{
			public Map<TypeDescription, Class<?>> load(DynamicClassLoader<CCTX, MCTX, SCTX, TCTX, ICTX> classLoader,
					Map<TypeDescription, byte[]> types)
			{
				return types
						.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> classLoader.defineClass(name, e.getValue(), 0, e.getValue().length)));
			}
		});

		Class<?> dynamicClass = dynamicType.getLoaded();
		invocationHandler.registerDynamicClassCreated(classContext, dynamicClass);

		return dynamicClass;
	}

	private DynamicType.Builder<?> defineDynamicConstructor(DynamicType.Builder<?> builder, CCTX classContext,
			MethodDescription constructor)
	{
		TCTX constructorContext = invocationHandler.createConstructorContext(classContext, constructor);
		ConstructorMethodHandler target = (receiver, args) -> invocationHandler.invokeConstructor(
				classContext, constructorContext, receiver, args);

		return builder
				.defineConstructor()
				.withParameters(constructor.getParameters().asTypeList())
				.intercept(MethodCall.invoke(Object_new).andThen(MethodCall
						.invoke(ConstructorMethodHandler_call)
						.on(target, ConstructorMethodHandler.class)
						.withThis()
						.withArgumentArray()
						.setsField(INSTANCE_CONTEXT_FIELD_MATCHER)));
	}

	private ReceiverTypeDefinition<?> defineDynamicMethod(DynamicType.Builder<?> builder, CCTX classContext, MethodDescription method)
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
			implementation = MethodCall.invoke(InstanceMethodHandler_call).on(target, InstanceMethodHandler.class).withThis().withField(INSTANCE_CONTEXT_FIELD_NAME);
		}

		return builder
				.defineMethod(method.getActualName(), method.getReturnType(),
						method.isStatic() ? List.of(Visibility.PUBLIC, Ownership.STATIC) : List.of(Visibility.PUBLIC))
				.withParameters(method.getParameters().asTypeList())
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
	public static interface ConstructorMethodHandler
	{
		// When renaming this method or changing its interface, remember to adjust corresponding Method constant
		public Object call(Object receiver, Object[] args);
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
