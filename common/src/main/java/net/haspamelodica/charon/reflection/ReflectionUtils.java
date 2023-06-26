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
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.haspamelodica.charon.OperationOutcome;

public class ReflectionUtils
{
	private static final Map<Class<?>, Class<?>> PRIMITIVE_CLASS_TO_BOX = Map.of(
			boolean.class, Boolean.class,
			char.class, Character.class,
			byte.class, Byte.class,
			short.class, Short.class,
			int.class, Integer.class,
			long.class, Long.class,
			float.class, Float.class,
			double.class, Double.class,
			void.class, Void.class);

	private static final Map<Class<?>, Class<?>> PRIMITIVE_BOX_TO_CLASS = PRIMITIVE_CLASS_TO_BOX.entrySet().stream()
			.collect(Collectors.toMap(Entry::getValue, Entry::getKey));

	private static final Set<Class<?>>	PRIMITIVE_CLASSES	= PRIMITIVE_CLASS_TO_BOX.keySet();
	private static final Set<Class<?>>	PRIMITIVE_BOXES		= PRIMITIVE_BOX_TO_CLASS.keySet();

	private static final Map<String, Class<?>> PRIMITIVE_CLASSES_BY_NAME = PRIMITIVE_CLASSES.stream()
			.collect(Collectors.toUnmodifiableMap(Class::getName, c -> c));

	private static final Map<Class<?>, Function<Object, List<?>>> PRIMITIVE_CLASS_TO_LIST_HANDLERS = Map.of(
			boolean.class, a -> IntStream.range(0, ((boolean[]) a).length).mapToObj(i -> ((boolean[]) a)[i]).toList(),
			char.class, a -> IntStream.range(0, ((char[]) a).length).mapToObj(i -> ((char[]) a)[i]).toList(),
			byte.class, a -> IntStream.range(0, ((byte[]) a).length).mapToObj(i -> ((byte[]) a)[i]).toList(),
			short.class, a -> IntStream.range(0, ((short[]) a).length).mapToObj(i -> ((short[]) a)[i]).toList(),
			int.class, a -> Arrays.stream((int[]) a).boxed().toList(),
			long.class, a -> Arrays.stream((long[]) a).boxed().toList(),
			float.class, a -> IntStream.range(0, ((float[]) a).length).mapToObj(i -> ((float[]) a)[i]).toList(),
			double.class, a -> Arrays.stream((double[]) a).boxed().toList());

	public static OperationOutcome<Object, Class<?>> newArray(Class<?> arrayType, int length)
	{
		try
		{
			// Don't try to cast to T[] (where T is the type argument to componentClass):
			// If componentClass is primitive, the resulting primitive array won't be an instance of Object[].
			return new OperationOutcome.Result<>(Array.newInstance(checkIsArrayTypeAndGetComponentType(arrayType), length));
		} catch(NegativeArraySizeException e)
		{
			return new OperationOutcome.ArraySizeNegative<>(length);
		}
	}

	public static OperationOutcome<Object, Class<?>> newMultiArray(Class<?> arrayType, List<Integer> dimensions)
	{
		try
		{
			int[] dimensionsArray = dimensions.stream().mapToInt(i -> i).toArray();

			Class<?> componentType = arrayType;
			for(int i = 0; i < dimensionsArray.length; i ++)
				componentType = checkIsArrayTypeAndGetComponentType(componentType);

			return new OperationOutcome.Result<>(Array.newInstance(componentType, dimensionsArray));
		} catch(NegativeArraySizeException e)
		{
			return new OperationOutcome.ArraySizeNegativeInMultiArray<>(dimensions);
		}
	}

	public static OperationOutcome<Object, Class<?>> newArrayWithInitialValues(Class<?> arrayType, List<Object> initialValues)
	{
		int length = initialValues.size();
		Object array = Array.newInstance(checkIsArrayTypeAndGetComponentType(arrayType), length);
		// index-based to defend against botched List implementations
		for(int i = 0; i < length; i ++)
			Array.set(array, i, initialValues.get(i));

		return new OperationOutcome.Result<>(array);
	}

	private static Class<?> checkIsArrayTypeAndGetComponentType(Class<?> arrayType)
	{
		if(!arrayType.isArray())
			throw new IllegalArgumentException("The given array type is not an array type: " + arrayType);

		return arrayType.getComponentType();
	}

	public static int getArrayLength(Object array)
	{
		return Array.getLength(array);
	}

	public static OperationOutcome<Object, Class<?>> getArrayElement(Object array, int index)
	{
		try
		{
			return new OperationOutcome.Result<>(Array.get(array, index));
		} catch(ArrayIndexOutOfBoundsException e)
		{
			return new OperationOutcome.ArrayIndexOutOfBounds<>(index, getArrayLength(array));
		}
	}

	public static OperationOutcome<Void, Class<?>> setArrayElement(Object array, int index, Object value)
	{
		try
		{
			Array.set(array, index, value);
			return new OperationOutcome.SuccessWithoutResult<>();
		} catch(ArrayIndexOutOfBoundsException e)
		{
			return new OperationOutcome.ArrayIndexOutOfBounds<>(index, getArrayLength(array));
		}
	}

	public static OperationOutcome<Object, Class<?>> callConstructor(Class<?> clazz, List<Class<?>> paramTypes, List<Object> args)
	{
		try
		{
			return new OperationOutcome.Result<>(callConstructorNoWrapReflectiveAction(clazz, paramTypes, args));
		} catch(InvocationTargetException e)
		{
			return new OperationOutcome.Thrown<>(e.getTargetException());
		} catch(NoSuchMethodException e)
		{
			return new OperationOutcome.ConstructorNotFound<>(clazz, paramTypes);
		} catch(InstantiationException e)
		{
			return new OperationOutcome.ConstructorOfAbstractClassCalled<>(clazz, paramTypes);
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		}
	}

	public static <R> R callConstructorNoWrapReflectiveAction(Class<R> clazz, List<Class<?>> paramTypes, List<Object> args)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		return clazz.getConstructor(paramTypes.toArray(Class[]::new)).newInstance(args.toArray());
	}

	public static OperationOutcome<Object, Class<?>> callStaticMethod(Class<?> clazz, String name, Class<?> returnType, List<Class<?>> paramTypes, List<Object> args)
	{
		try
		{
			return new OperationOutcome.Result<>(lookupMethod(clazz, true, name, paramTypes, returnType).invoke(null, args.toArray()));
		} catch(InvocationTargetException e)
		{
			return new OperationOutcome.Thrown<Object, Class<?>>(e.getTargetException());
		} catch(NoSuchMethodException e)
		{
			return new OperationOutcome.MethodNotFound<>(clazz, name, returnType, paramTypes, true);
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		}
	}

	public static OperationOutcome<Object, Class<?>> getStaticField(Class<?> clazz, String name, Class<?> fieldType)
	{
		try
		{
			return new OperationOutcome.Result<>(lookupField(clazz, name, fieldType).get(null));
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		} catch(NoSuchFieldException e)
		{
			return new OperationOutcome.FieldNotFound<>(clazz, name, fieldType, true);
		}
	}

	public static <F> OperationOutcome<Void, Class<?>> setStaticField(Class<?> clazz, String name, Class<F> fieldType, F value)
	{
		try
		{
			lookupField(clazz, name, fieldType).set(null, value);
			return new OperationOutcome.SuccessWithoutResult<>();
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		} catch(NoSuchFieldException e)
		{
			return new OperationOutcome.FieldNotFound<Void, Class<?>>(clazz, name, fieldType, true);
		}
	}

	public static <T> OperationOutcome<Object, Class<?>> callInstanceMethod(Class<T> clazz, String name, Class<?> returnType, List<Class<?>> paramTypes,
			T receiver, List<Object> args)
	{
		try
		{
			//TODO check if is instance method
			//TODO this causes problems when an inaccessible class overrides an accessible method.
			// Or just always setAccessible?
			return new OperationOutcome.Result<>(lookupMethod(clazz, false, name, paramTypes, returnType).invoke(receiver, args.toArray()));
		} catch(InvocationTargetException e)
		{
			return new OperationOutcome.Thrown<Object, Class<?>>(e.getTargetException());
		} catch(NoSuchMethodException e)
		{
			return new OperationOutcome.MethodNotFound<Object, Class<?>>(clazz, name, returnType, paramTypes, false);
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		}
	}

	public static <T> OperationOutcome<Object, Class<?>> getInstanceField(Class<T> clazz, String name, Class<?> fieldType, T receiver)
	{
		try
		{
			//TODO check if is instance field
			return new OperationOutcome.Result<>(lookupField(clazz, name, fieldType).get(receiver));
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		} catch(NoSuchFieldException e)
		{
			return new OperationOutcome.FieldNotFound<Object, Class<?>>(clazz, name, fieldType, false);
		}
	}

	public static <T, F> OperationOutcome<Void, Class<?>> setInstanceField(Class<T> clazz, String name, Class<F> fieldType, T receiver, F value)
	{
		try
		{
			//TODO check if is instance field
			lookupField(clazz, name, fieldType).set(receiver, value);
			return new OperationOutcome.SuccessWithoutResult<>();
		} catch(IllegalAccessException e)
		{
			return handleIllegalAccessException(e);
		} catch(NoSuchFieldException e)
		{
			return new OperationOutcome.FieldNotFound<Void, Class<?>>(clazz, name, fieldType, false);
		}
	}

	private static Field lookupField(Class<?> clazz, String name, Class<?> fieldType) throws NoSuchFieldException
	{
		Field field = clazz.getDeclaredField(name);
		if(!field.getType().equals(fieldType))
			throw new NoSuchFieldException("Field was found, but type mismatches: expected " + fieldType + ", but field is " + field);
		// isAccessible is deprecated. We can tolerate the inefficiency generated by always setting accessible.
		field.setAccessible(true);
		return field;
	}

	private static Method lookupMethod(Class<?> clazz, boolean isStatic, String name,
			List<Class<?>> paramTypes, Class<?> returnType) throws NoSuchMethodException
	{
		Method method = lookupMethodIgnoreReturnTypeAndStatic(clazz, name, paramTypes);

		if(!method.getReturnType().equals(returnType))
			throw new NoSuchMethodException(
					"Method was found, but return type mismatches: expected " + returnType + ", but method is " + method);
		if(isStatic != Modifier.isStatic(method.getModifiers()))
			throw new NoSuchMethodException(
					"Method was found, but whether it is static or not mismatches: expected " + isStatic + ", but method is " + method);

		return method;
	}

	private static Method lookupMethodIgnoreReturnTypeAndStatic(Class<?> clazz, String name, List<Class<?>> paramTypes) throws NoSuchMethodException
	{
		NoSuchMethodException notFoundException;

		// First, try to find the method with getMethod. This will only find public methods,
		// but include methods of superclasses / superinterfaces.
		try
		{
			return clazz.getMethod(name, paramTypes.toArray(Class[]::new));
		} catch(NoSuchMethodException e)
		{
			// Save this NoSuchMethodException to be able to throw it later,
			// as this exception probably has the best error message.
			notFoundException = e;
		}

		// We know the method we are trying to find is not public.
		// This means it couldn't be originally declared in an interface,
		// because all interface methods are public. So we only have to search
		// for non-public methods in superclasses, not superinterfaces.
		for(Class<?> currentClazz = clazz; currentClazz != null; currentClazz = currentClazz.getSuperclass())
		{
			Method method;
			try
			{
				method = clazz.getDeclaredMethod(name, paramTypes.toArray(Class[]::new));
			} catch(NoSuchMethodException e)
			{
				// ignore exception; continue with next higher superclass
				continue;
			}
			// isAccessible is deprecated. We can tolerate the inefficiency generated by always setting accessible.
			method.setAccessible(true);
		}

		// We weren't able to find a matching method in any superclass.
		// This means the searched method doesn't exist.
		// We throw the NoSuchMethodException originally thrown by getMethod,
		// because that one probably has the best error message.
		throw notFoundException;
	}

	public static <T> T castOrPrimitive(Class<T> clazz, Object obj)
	{
		if(!clazz.isPrimitive())
			//TODO this causes a ClassCastException for the exercise creator in the following situation:
			// - There are two classes A and B on the student side; along with their exercise-side SSIs.
			// - The student-side B extends A, but the exercise-side (SSI of) B does not extend (the SSI of) A (this is crucial).
			// - There is a method with return type A on the student side; along with its exercise-side representation (also declared to return A)
			// - A call of the method returns an object of type B.
			return clazz.cast(obj);

		Class<?> box = getBoxOfPrimitiveType(clazz);
		// For primitives, primivite.class has type Class<PrimitiveBox>.
		// So, if we get passed primitive.class, T is PrimitiveBox.
		@SuppressWarnings("unchecked")
		Class<T> boxCasted = (Class<T>) box;
		return boxCasted.cast(obj);
	}

	public static boolean isPrimitiveName(String classname)
	{
		return PRIMITIVE_CLASSES_BY_NAME.containsKey(classname);
	}

	public static <T> Class<?> getBoxOfPrimitiveType(Class<T> clazz)
	{
		return PRIMITIVE_CLASS_TO_BOX.get(clazz);
	}

	public static boolean isBoxedPrimitive(Class<?> clazz)
	{
		return PRIMITIVE_BOXES.contains(clazz);
	}

	public static Class<?> getPrimitiveTypeOfBox(Class<?> clazz)
	{
		return PRIMITIVE_BOX_TO_CLASS.get(clazz);
	}

	public static Function<Object, List<?>> arrayToListHandlersHandlingPrimitives(Class<?> componentType)
	{
		return componentType.isPrimitive() ? PRIMITIVE_CLASS_TO_LIST_HANDLERS.get(componentType) : a -> List.of((Object[]) a);
	}

	public static OperationOutcome<Object, Class<?>> nameToClassWrapReflectiveAction(String classname)
	{
		return nameToClassWrapReflectiveAction(classname, null);
	}
	public static OperationOutcome<Object, Class<?>> nameToClassWrapReflectiveAction(String classname, ClassLoader classloader)
	{
		try
		{
			return new OperationOutcome.Result<>(nameToClass(classname, classloader));
		} catch(ClassNotFoundException e)
		{
			return new OperationOutcome.ClassNotFound<>(classname);
		}
	}
	public static Class<?> nameToClass(String classname) throws ClassNotFoundException
	{
		return nameToClass(classname, null);
	}
	public static Class<?> nameToClass(String classname, ClassLoader classloader) throws ClassNotFoundException
	{
		Class<?> primitiveClass = primitiveNameToClassOrNull(classname);
		return primitiveClass != null ? primitiveClass : Class.forName(classname, true,
				classloader != null ? classloader : ReflectionUtils.class.getClassLoader());
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

	private static <R> R handleIllegalAccessException(IllegalAccessException e)
	{
		//TODO better exception type
		throw new RuntimeException("Charon wasn't able to access member", e);
	}

	private ReflectionUtils()
	{}
}
