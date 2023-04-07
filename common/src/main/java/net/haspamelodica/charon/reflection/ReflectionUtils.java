package net.haspamelodica.charon.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionUtils
{
	private static final Map<Class<?>, Class<?>> PRIMITIVE_CLASS_WRAPPERS = Map.of(
			boolean.class, Boolean.class,
			char.class, Character.class,
			byte.class, Byte.class,
			short.class, Short.class,
			int.class, Integer.class,
			long.class, Long.class,
			float.class, Float.class,
			double.class, Double.class,
			void.class, Void.class);

	private static final Set<Class<?>>			PRIMITIVE_CLASSES			= PRIMITIVE_CLASS_WRAPPERS.keySet();
	private static final Map<String, Class<?>>	PRIMITIVE_CLASSES_BY_NAME	= PRIMITIVE_CLASSES.stream()
			.collect(Collectors.toUnmodifiableMap(Class::getName, c -> c));

	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(Class<T> componentClass, int length)
	{
		return (T[]) Array.newInstance(componentClass, length);
	}

	public static Object newMultiArray(Class<?> componentClass, List<Integer> dimensions)
	{
		return Array.newInstance(componentClass, dimensions.stream().mapToInt(i -> i).toArray());
	}

	public static <T> T callConstructor(Class<T> clazz, List<Class<?>> paramTypes, List<Object> args) throws ExceptionInTargetException
	{
		return doChecked(() -> clazz.getConstructor(paramTypes.toArray(Class[]::new)).newInstance(args.toArray()));
	}

	public static <R> R callStaticMethod(Class<?> clazz, String name, Class<R> returnType, List<Class<?>> paramTypes,
			List<Object> args) throws ExceptionInTargetException
	{
		return doChecked(() ->
		{
			Method method = lookupMethod(clazz, true, name, paramTypes, returnType);
			@SuppressWarnings("unchecked") // we checked the class manually
			R result = (R) method.invoke(null, args.toArray());
			return result;
		});
	}

	public static <F> F getStaticField(Class<?> clazz, String name, Class<F> fieldType)
	{
		return doCheckedNoInvocationTargetException(() ->
		{
			Field field = lookupField(clazz, name, fieldType);
			@SuppressWarnings("unchecked") // we checked the class manually
			F result = (F) field.get(null);
			return result;
		});
	}

	public static <F> void setStaticField(Class<?> clazz, String name, Class<F> fieldType, F value)
	{
		doCheckedNoInvocationTargetException(() -> lookupField(clazz, name, fieldType).set(null, value));
	}

	public static <T, R> R callInstanceMethod(Class<T> clazz, String name, Class<R> returnType, List<Class<?>> paramTypes,
			T receiver, List<Object> args) throws ExceptionInTargetException
	{
		return doChecked(() ->
		{
			Method method = lookupMethod(clazz, false, name, paramTypes, returnType);
			//TODO check if is instance method
			@SuppressWarnings("unchecked") // we checked the class manually
			R result = (R) method.invoke(receiver, args.toArray());
			return result;
		});
	}

	public static <T, F> F getInstanceField(Class<T> clazz, String name, Class<F> fieldType, T receiver)
	{
		return doCheckedNoInvocationTargetException(() ->
		{
			Field field = lookupField(clazz, name, fieldType);
			//TODO check if is instance field
			@SuppressWarnings("unchecked") // we checked the class manually
			F result = (F) field.get(receiver);
			return result;
		});
	}

	public static <T, F> void setInstanceField(Class<T> clazz, String name, Class<F> fieldType, T receiver, F value)
	{
		//TODO check if is instance field
		doCheckedNoInvocationTargetException(() -> lookupField(clazz, name, fieldType).set(receiver, value));
	}

	private static <F> Field lookupField(Class<?> clazz, String name, Class<F> fieldType) throws NoSuchFieldException, ClassNotFoundException
	{
		Field field = clazz.getDeclaredField(name);
		if(!field.getType().equals(fieldType))
			throw new NoSuchFieldException("Field was found, but type mismatches: expected " + fieldType + ", but field is " + field);
		// isAccessible is deprecated. We can tolerate the inefficiency generated by always setting accessible.
		field.setAccessible(true);
		return field;
	}

	private static Method lookupMethod(Class<?> clazz, boolean isStatic, String name,
			List<Class<?>> paramTypes, Class<?> returnType) throws NoSuchMethodException, ClassNotFoundException
	{
		Method method = clazz.getDeclaredMethod(name, paramTypes.toArray(Class[]::new));
		if(!method.getReturnType().equals(returnType))
			throw new NoSuchMethodException(
					"Method was found, but return type mismatches: expected " + returnType + ", but method is " + method);
		if(isStatic != Modifier.isStatic(method.getModifiers()))
			throw new NoSuchMethodException(
					"Method was found, but whether it is static or not mismatches: expected " + isStatic + ", but method is " + method);
		// isAccessible is deprecated. We can tolerate the inefficiency generated by always setting accessible.
		method.setAccessible(true);
		return method;
	}

	public static <T> T castOrPrimitive(Class<T> clazz, Object obj)
	{
		if(!clazz.isPrimitive())
			return clazz.cast(obj);

		Class<?> wrapper = PRIMITIVE_CLASS_WRAPPERS.get(clazz);
		// For primitives, primivite.class has type Class<PrimitiveWrapper>.
		// So, if we get passed primitive.class, T is PrimitiveWrapper.
		@SuppressWarnings("unchecked")
		Class<T> wrapperCasted = (Class<T>) wrapper;
		return wrapperCasted.cast(obj);
	}

	public static boolean isPrimitiveName(String classname)
	{
		return PRIMITIVE_CLASSES_BY_NAME.containsKey(classname);
	}

	public static List<Class<?>> nameToClass(List<String> classnames)
	{
		return classnames.stream().<Class<?>> map(ReflectionUtils::nameToClass).toList();
	}
	public static Class<?> nameToClass(String classname)
	{
		return nameToClass(classname, null);
	}
	public static Class<?> nameToClass(String classname, ClassLoader classloader)
	{
		return doCheckedNoInvocationTargetException(n ->
		{
			Class<?> primitiveClass = primitiveNameToClassOrNull(n);
			return primitiveClass != null ? primitiveClass : Class.forName(n, true,
					classloader != null ? classloader : ReflectionUtils.class.getClassLoader());
		}, classname);
	}
	public static Class<?> primitiveNameToClassOrThrow(String classname)
	{
		Class<?> result = primitiveNameToClassOrNull(classname);
		if(result == null)
			//TODO better exception type
			throw new IllegalArgumentException("Not a primitive class: " + classname);
		return result;
	}
	public static Class<?> primitiveNameToClassOrNull(String classname)
	{
		return PRIMITIVE_CLASSES_BY_NAME.get(classname);
	}

	public static List<String> classToName(Class<?>[] classes)
	{
		return Arrays.stream(classes).map(ReflectionUtils::classToName).toList();
	}
	public static List<String> classToName(List<Class<?>> classes)
	{
		return classes.stream().map(ReflectionUtils::classToName).toList();
	}
	public static String classToName(Class<?> clazz)
	{
		return clazz.getName();
	}

	public static <P> P createProxyInstance(Class<P> proxiedClass, InvocationHandler handler)
	{
		Object proxyInstance = Proxy.newProxyInstance(proxiedClass.getClassLoader(), new Class[] {proxiedClass}, handler);
		@SuppressWarnings("unchecked")
		P proxyInstanceCasted = (P) proxyInstance;
		return proxyInstanceCasted;
	}

	public static List<Object> argsToList(Object[] args)
	{
		return args == null ? List.of() : Arrays.asList(args);
	}

	public static void doChecked(UnhandledReflectiveRunnable body) throws ExceptionInTargetException
	{
		doChecked(() ->
		{
			body.run();
			return null;
		});
	}

	public static <R> R doChecked(UnhandledReflectiveSupplier<R> body) throws ExceptionInTargetException
	{
		return doChecked(a -> body.get(), null);
	}

	public static <A, R> R doChecked(UnhandledReflectiveFunction<A, R> body, A a) throws ExceptionInTargetException
	{
		try
		{
			return body.apply(a);
		} catch(InstantiationException | IllegalAccessException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e)
		{
			return handleReflectiveOperationException(e);
		} catch(InvocationTargetException e)
		{
			throw new ExceptionInTargetException(e.getTargetException());
		}
	}

	public static void doCheckedNoInvocationTargetException(UnhandledReflectiveRunnableNoInvocationTargetException body)
	{
		doCheckedNoInvocationTargetException(() ->
		{
			body.run();
			return null;
		});
	}

	public static <R> R doCheckedNoInvocationTargetException(UnhandledReflectiveSupplierNoInvocationTargetException<R> body)
	{
		return doCheckedNoInvocationTargetException(a -> body.get(), null);
	}

	public static <A, R> R doCheckedNoInvocationTargetException(UnhandledReflectiveFunctionNoInvocationTargetException<A, R> body, A a)
	{
		try
		{
			return body.apply(a);
		} catch(InstantiationException | IllegalAccessException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e)
		{
			return handleReflectiveOperationException(e);
		}
	}

	private static <R> R handleReflectiveOperationException(ReflectiveOperationException e)
	{
		//TODO use better exception type
		throw new RuntimeException(e);
	}

	private ReflectionUtils()
	{}
}
