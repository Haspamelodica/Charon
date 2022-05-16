package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.ModifierContributor.ForMethod;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class CharonClassLoader extends TransformingClassLoader
{
	private static final Constructor<?>	objectConstructor;
	private static final Method			methodInvoked;
	private static final Method			constructorInvoked;
	static
	{
		try
		{
			objectConstructor = Object.class.getConstructor();
			methodInvoked = CharonClassLoader.class.getDeclaredMethod("methodInvoked", Class.class, String.class, MethodInterface.class, Object.class, Object[].class);
			constructorInvoked = CharonClassLoader.class.getDeclaredMethod("constructorInvoked", Class.class, String.class, ConstructorInterface.class, Object[].class);
		} catch(NoSuchMethodException | SecurityException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	private final ExpectedInterfaceProvider	expectedInterfaceProvider;
	private final boolean					preferOriginalClasses;

	public CharonClassLoader(ExpectedInterfaceProvider expectedInterfaceProvider, boolean preferOriginalClasses)
	{
		this.expectedInterfaceProvider = expectedInterfaceProvider;
		this.preferOriginalClasses = preferOriginalClasses;
	}

	@Override
	protected Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL)
	{
		System.out.println("Loading " + name);
		// Delegate MethodInterface and similar classes to parent; don't define it ourself:
		// An instance of MethodInterface is stored in dynamically-generated classes.
		// The instance of CharonClassLoader uses the MethodInterface class by the parent classloader,
		// but the dynamically-generated class the MethodInterface class of CharonClassLoader.
		// This would cause ClassCastExceptions.
		// (A String (the classname) is saved as well, but Strings are not redefinable anyway.)
		if(name.equals(MethodInterface.class.getName()) ||
				name.equals(ConstructorInterface.class.getName()) ||
				name.equals(CharonClassLoader.class.getName()))
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
		ClassInterface expectedInterface = expectedInterfaceProvider.expectedInterfaceFor(name);
		return expectedInterface == null ? null : mockClassWithInterface(name, expectedInterface);
	}

	private Class<?> defineClassOrNull(String name, byte[] bytes) throws ClassFormatError
	{
		return bytes == null ? null : defineClass(name, bytes, 0, bytes.length);
	}

	private Class<?> mockClassWithInterface(String name, ClassInterface expectedInterface)
	{
		DynamicType.Builder<?> builder = new ByteBuddy()
				//TODO modify here if we support inheritance
				.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.name(name);

		for(ConstructorInterface constructor : expectedInterface.constructors())
		{
			builder = builder
					.defineConstructor()
					.withParameters(constructor.parameterTypes())
					.intercept(MethodCall.invoke(objectConstructor)
							.andThen(MethodCall.invoke(constructorInvoked).on(this).withOwnType().with(name, constructor).withArgumentArray()));
		}

		for(MethodInterface method : expectedInterface.methods())
		{
			List<ForMethod> modifiers = method.isStatic() ? List.of(Visibility.PUBLIC, Ownership.STATIC) : List.of(Visibility.PUBLIC);
			MethodCall implementationBuilder = MethodCall.invoke(methodInvoked).on(this).withOwnType().with(name, method);
			implementationBuilder = method.isStatic() ? implementationBuilder.with((Object) null) : implementationBuilder.withThis();
			builder = builder
					.defineMethod(method.name(), method.returnType(), modifiers)
					.withParameters(method.parameters())
					.intercept(implementationBuilder.withArgumentArray().withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
		}

		DynamicType.Unloaded<?> unloaded = builder.make();
		DynamicType.Loaded<?> dynamicType = unloaded.load(this, new ClassLoadingStrategy<>()
		{
			public Map<TypeDescription, Class<?>> load(CharonClassLoader classLoader, Map<TypeDescription, byte[]> types)
			{
				return types
						.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, e -> classLoader.defineClass(name, e.getValue(), 0, e.getValue().length)));
			}
		});
		return dynamicType.getLoaded();
	}

	// This method is not unused: it is used reflectively.
	public Object methodInvoked(Class<?> clazz, String classname, MethodInterface method, Object receiver, Object[] args)
	{
		//TODO debug code
		System.out.println("methodInvoked: <" + classname + "> " + receiver + "." + method.name() + "(" + Arrays.toString(args) + ")");
		String returnClassname = method.returnType().getTypeName();
		return switch(returnClassname.charAt(returnClassname.length() - 1))
		{
			case 'A', 'B' -> ((Supplier<?>) args[0]).get();
			default -> null;
		};
	}

	// This method is not unused: it is used reflectively.
	public void constructorInvoked(Class<?> clazz, String classname, ConstructorInterface constructor, Object[] args)
	{
		//TODO debug code
		System.out.println("constructorInvoked: new " + classname + "(" + Arrays.toString(args) + ")");
	}
}
