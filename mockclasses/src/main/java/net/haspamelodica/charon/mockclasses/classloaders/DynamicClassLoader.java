package net.haspamelodica.charon.mockclasses.classloaders;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

public class DynamicClassLoader<CCTX, MCTX, SCTX, TCTX, ICTX> extends ClassLoader
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

	private final DynamicInterfaceProvider	interfaceProvider;
	private final DynamicClassTransformer	transformer;

	private final DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler;

	public DynamicClassLoader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider,
			DynamicClassTransformer transformer, DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler)
	{
		super(parent);
		this.interfaceProvider = interfaceProvider;
		this.transformer = transformer;
		this.invocationHandler = invocationHandler;
	}


	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		TypeDefinition typeDefinition = interfaceProvider.typeDefinitionFor(name);
		if(typeDefinition == null)
			throw new ClassNotFoundException(name);
		return mockType(name, typeDefinition);
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
				.defineConstructor(Visibility.PUBLIC)
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
