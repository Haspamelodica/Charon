package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkReturnAndParameterTypes;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.defaultHandler;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getStudentSideName;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSSI;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSerializable;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSerializableSSI;

public final class StudentSidePrototypeBuilder<REF, TYPEREF extends REF, SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
{
	public final Class<SP>	prototypeClass;
	public final Class<SI>	instanceClass;

	private final ParameterizedType				parameterizedPrototypeBaseClass;
	public final PrototypeVariant				prototypeVariant;
	public final Class<?>						componentSSIType;
	public final Class<?>						componentSerializableType;
	private final Class<? extends SerDes<?>>	arrayElementSerdes;

	public final MarshalingCommunicator<REF, TYPEREF, StudentSideException> prototypeWideMarshalingCommunicator;

	public final StudentSideTypeImpl<REF, TYPEREF> studentSideType;

	public final StudentSideInstanceBuilder<REF, TYPEREF, SI> instanceBuilder;

	private final Map<Method, MethodHandler> methodHandlers;

	public final SP prototype;

	public StudentSidePrototypeBuilder(MarshalingCommunicator<REF, TYPEREF, StudentSideException> globalMarshalingCommunicator, Class<SP> prototypeClass)
	{
		// The order of the following operations is important: (almost) each step depends on the last
		this.prototypeClass = prototypeClass;
		InfoFromPrototypeClass<SI> infoFromPrototypeClass = checkPrototypeClassAndGetInstanceClass();
		this.instanceClass = infoFromPrototypeClass.instanceClass();
		this.parameterizedPrototypeBaseClass = infoFromPrototypeClass.parameterizedPrototypeBaseClass();
		this.prototypeVariant = infoFromPrototypeClass.prototypeVariant();
		this.componentSSIType = getSSIComponentType();
		this.componentSerializableType = getSerializableComponentType();
		this.arrayElementSerdes = getArrayElementSerdes();
		this.prototypeWideMarshalingCommunicator = createPrototypeWideMarshalingCommunicator(globalMarshalingCommunicator);
		this.studentSideType = lookupAndVerifyStudentSideType();
		this.instanceBuilder = new StudentSideInstanceBuilder<>(this);
		this.methodHandlers = createMethodHandlers();
		this.prototype = createPrototype();
	}

	private static record InfoFromPrototypeClass<SI>(ParameterizedType parameterizedPrototypeBaseClass, Class<SI> instanceClass,
			PrototypeVariant prototypeVariant)
	{}
	private InfoFromPrototypeClass<SI> checkPrototypeClassAndGetInstanceClass()
	{
		if(!prototypeClass.isInterface())
			throw new InconsistentHierarchyException("Prototype classes have to be interfaces: " + prototypeClass);

		checkNotAnnotatedWith(prototypeClass, OverrideStudentSideName.class);
		checkNotAnnotatedWith(prototypeClass, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSideInstanceMethodKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSidePrototypeMethodKind.class);

		if(StudentSideInstance.class.isAssignableFrom(prototypeClass))
			throw new InconsistentHierarchyException("A prototype class should not implement "
					+ StudentSideInstance.class.getSimpleName() + ": " + prototypeClass);

		InfoFromPrototypeClass<SI> info = null;

		for(Type genericSuperinterface : prototypeClass.getGenericInterfaces())
			if(genericSuperinterface.equals(StudentSidePrototype.class))
				throw new InconsistentHierarchyException("A prototype class has to give a type argument to "
						+ StudentSidePrototype.class.getSimpleName() + ": " + prototypeClass);
			else if(genericSuperinterface instanceof ParameterizedType parameterizedSuperinterface)
				for(PrototypeVariant variant : PrototypeVariant.values())
					info = updatePrototypeInfo(info, parameterizedSuperinterface, variant);

		if(info == null)
			throw new InconsistentHierarchyException("A prototype class has to implement exactly one of "
					+ Arrays.stream(PrototypeVariant.values()).map(PrototypeVariant::prototypeBaseClass)
							.map(Class::getSimpleName).collect(Collectors.joining(", "))
					+ " directly: " + prototypeClass);

		return info;
	}

	private InfoFromPrototypeClass<SI> updatePrototypeInfo(InfoFromPrototypeClass<SI> oldInfo,
			ParameterizedType parameterizedSuperinterface, PrototypeVariant prototypeVariant)
	{
		if(parameterizedSuperinterface.getRawType() != prototypeVariant.prototypeBaseClass())
			return oldInfo;

		if(oldInfo != null)
			throw new InconsistentHierarchyException("Prototype class is prototype in multiple ways: "
					+ prototypeVariant.prototypeBaseClass().getSimpleName() + " and at least one other: " + prototypeClass);

		// From the type signature of StudentSidePrototype (and its array-related subclasses), we know SP's type parameter to StudentSidePrototype is SI.
		// So, this cast has to succeed.
		@SuppressWarnings("unchecked")
		Class<SI> instanceClass = (Class<SI>) getNthTypeArgumentAndCheckIsClass(parameterizedSuperinterface,
				prototypeVariant.prototypeBaseClass(), prototypeVariant.arrayTypeArgumentIndex());

		return new InfoFromPrototypeClass<>(parameterizedSuperinterface, instanceClass, prototypeVariant);
	}

	private Class<?> getSSIComponentType()
	{
		return getNthTypeArgumentOfPrototypeClassAndCheckIsClassOrNull(prototypeVariant.ssiComponentTypeArgumentIndex());
	}

	private Class<?> getSerializableComponentType()
	{
		return getNthTypeArgumentOfPrototypeClassAndCheckIsClassOrNull(prototypeVariant.serializableComponentTypeArgumentIndex());
	}

	private Class<? extends SerDes<?>> getArrayElementSerdes()
	{
		// From the type signature of StudentSidePrototype (and its array-related subclasses), we know SP's type parameter to StudentSidePrototype is SI.
		// So, this cast has to succeed.
		@SuppressWarnings("unchecked")
		Class<? extends SerDes<?>> result = (Class<? extends SerDes<?>>) getNthTypeArgumentOfPrototypeClassAndCheckIsClassOrNull(prototypeVariant.arrayElementSerdesArgumentIndex());
		return result;
	}

	private MarshalingCommunicator<REF, TYPEREF, StudentSideException> createPrototypeWideMarshalingCommunicator(
			MarshalingCommunicator<REF, TYPEREF, StudentSideException> globalMarshalingCommunicator)
	{
		MarshalingCommunicator<REF, TYPEREF, StudentSideException> withSerDesesFromPrototypeAndInstanceClass = globalMarshalingCommunicator
				.withAdditionalSerDeses(getSerDeses(prototypeClass))
				.withAdditionalSerDeses(getSerDeses(instanceClass));

		if(arrayElementSerdes == null)
			return withSerDesesFromPrototypeAndInstanceClass;

		return withSerDesesFromPrototypeAndInstanceClass.withAdditionalSerDeses(List.of(arrayElementSerdes));
	}

	private StudentSideTypeImpl<REF, TYPEREF> lookupAndVerifyStudentSideType()
	{
		return new StudentSideTypeImpl<>(prototypeWideMarshalingCommunicator,
				prototypeVariant.isArray() ? lookupAndVerifyTyperefForArray() : lookupAndVerifyTyperefForNonarray());
	}

	private TYPEREF lookupAndVerifyTyperefForArray()
	{
		return prototypeWideMarshalingCommunicator.getArrayType(lookupAndVerifyComponentTyperefForArray());
	}

	private TYPEREF lookupAndVerifyComponentTyperefForArray()
	{
		TYPEREF componentTyperefViaSSI = componentSSIType == null
				? null
				: prototypeWideMarshalingCommunicator.getTypeByNameAndVerify(getStudentSideName(componentSSIType));

		TYPEREF componentTyperefViaSerializable = prototypeWideMarshalingCommunicator.getTypeHandledByStudentSideSerdes(arrayElementSerdes);

		if(componentTyperefViaSSI == null)
			return componentTyperefViaSerializable;

		if(componentTyperefViaSerializable == null)
			return componentTyperefViaSSI;

		if(componentTyperefViaSSI != componentTyperefViaSerializable)
			throw new ExerciseCausedException("The SSI and Serializable components for an array don't resolve to the same student-side type: " + prototypeClass);

		return componentTyperefViaSSI;
	}

	private TYPEREF lookupAndVerifyTyperefForNonarray()
	{
		return prototypeWideMarshalingCommunicator.getTypeByNameAndVerify(getStudentSideName(instanceClass));
	}

	private SP createPrototype()
	{
		return createProxyInstance(prototypeClass, (proxy, method, args) -> methodHandlers.get(method).invoke(proxy, args));
	}

	private record MethodWithHandler(Method method, MethodHandler handler)
	{}
	private Map<Method, MethodHandler> createMethodHandlers()
	{
		return Stream.concat(ssiOrArrayMethodHandlers(), objectMethodHandlers())
				.collect(Collectors.toUnmodifiableMap(MethodWithHandler::method, MethodWithHandler::handler));
	}

	private Stream<MethodWithHandler> objectMethodHandlers()
	{
		// Yes, those methods could be overloaded. But even so, we wouldn't know what to do with the overloaded variants.
		// So, throwing an exception (via checkReturnAndParameterTypes) is appropriate.
		return Arrays.stream(Object.class.getMethods())
				.filter(method -> !Modifier.isFinal(method.getModifiers()))
				.map(method -> switch(method.getName())
				{
					case "toString" -> new MethodWithHandler(checkReturnAndParameterTypes(method, String.class),
							(proxy, args) -> "Prototype[" + studentSideType.name() + "]");
					case "hashCode" -> new MethodWithHandler(checkReturnAndParameterTypes(method, int.class),
							(proxy, args) -> System.identityHashCode(proxy));
					case "equals" -> new MethodWithHandler(checkReturnAndParameterTypes(method, boolean.class, Object.class),
							(proxy, args) -> proxy == args[0]);
					default -> throw new FrameworkCausedException("Unknown method of Object: " + method);
				});
	}

	private Stream<MethodWithHandler> ssiOrArrayMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods with getMethods(): abstract interface methods have to be public
		return Arrays.stream(prototypeClass.getMethods())
				.map(prototypeVariant.isArray() ? this::arrayMethodHandlerFor : m -> new MethodWithHandler(m, ssiMethodHandlerFor(m)));
	}

	private MethodWithHandler arrayMethodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSideInstanceMethodKind.class);

		if(!Modifier.isAbstract(method.getModifiers()))
		{
			checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
			return new MethodWithHandler(method, defaultHandler(method));
		}

		if(method.getDeclaringClass() != prototypeVariant.prototypeBaseClass())
			throw new InconsistentHierarchyException("An array prototype can't define new abstract methods: " + prototypeClass);

		return switch(method.getName())
		{
			case "createArray" -> new MethodWithHandler(method, createArrayHandler(method, false));
			case "createArrayFromSerializable" -> new MethodWithHandler(method, createArrayHandler(method, true));
			default -> throw new FrameworkCausedException("Unknown method of " + prototypeVariant.prototypeBaseClass() + ": " + method);
		};
	}

	private MethodHandler createArrayHandler(Method method, boolean hasToBeListOfSerializable)
	{
		// Unfortunately we can't easily check the return type because it's generic.

		if(method.getParameterCount() != 1)
			throw new FrameworkCausedException("Array creation method doesn't have exactly one parameter: " + prototypeClass);

		Class<?> parameterTypeErasure = method.getParameters()[0].getType();

		if(parameterTypeErasure == List.class)
		{
			Class<?> componentType = hasToBeListOfSerializable || prototypeVariant == PrototypeVariant.ARRAY_SERIALIZABLE
					? componentSerializableType
					: componentSSIType;
			return (proxy, args) ->
			{
				@SuppressWarnings("unchecked")
				List<Object> initialValues = (List<Object>) args[0];
				return prototypeWideMarshalingCommunicator.newArrayWithInitialValues(instanceClass, componentType, initialValues);
			};
		}

		if(hasToBeListOfSerializable)
			throw new FrameworkCausedException("Serializable-based array creation method's parameter had unexpected type "
					+ parameterTypeErasure + ": " + prototypeClass);

		if(parameterTypeErasure == int.class)
			return (proxy, args) -> prototypeWideMarshalingCommunicator.newArray(instanceClass, studentSideType.getTyperef(), (Integer) args[0]);

		if(parameterTypeErasure == int[].class)
			return (proxy, args) -> prototypeWideMarshalingCommunicator.newMultiArray(instanceClass, studentSideType.getTyperef(),
					Arrays.stream((int[]) args[0]).boxed().toList());

		if(Object[].class.isAssignableFrom(parameterTypeErasure))
		{
			Class<?> componentType = chooseComponentTypeFromErasure(parameterTypeErasure.getComponentType(), method);

			return (proxy, args) -> prototypeWideMarshalingCommunicator.newArrayWithInitialValues(instanceClass, componentType,
					Arrays.asList((Object[]) args[0]));
		}

		throw new FrameworkCausedException("Array creation method's parameter had unexpected type " + parameterTypeErasure + ": " + prototypeClass);
	}

	private MethodHandler ssiMethodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSideInstanceMethodKind.class);
		MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator =
				prototypeWideMarshalingCommunicator.withAdditionalSerDeses(getSerDeses(method));

		return handlerFor(method, StudentSidePrototypeMethodKind.class,
				(kind, name, nameOverridden) -> switch(kind.value())
				{
					case CONSTRUCTOR -> constructorHandler(method, methodWideMarshalingCommunicator, nameOverridden);
					case STATIC_METHOD -> staticMethodHandler(method, methodWideMarshalingCommunicator, name);
					case STATIC_FIELD_GETTER -> staticFieldGetterHandler(method, methodWideMarshalingCommunicator, name);
					case STATIC_FIELD_SETTER -> staticFieldSetterHandler(method, methodWideMarshalingCommunicator, name);
				});
	}

	private MethodHandler constructorHandler(Method method, MarshalingCommunicator<REF, TYPEREF, StudentSideException> marshalingCommunicator,
			boolean nameOverridden)
	{
		if(instanceBuilder.kind != Kind.CLASS)
			throw new InconsistentHierarchyException("Only student-side classes can have constructors: " + method);

		if(nameOverridden)
			throw new InconsistentHierarchyException("Student-side constructor had name overridden: " + method);

		if(!method.getReturnType().equals(instanceClass))
			throw new InconsistentHierarchyException("Student-side constructor return type wasn't the associated student-side instance class: " +
					"expected " + instanceClass + ", but was " + method.getReturnType() + ": " + method);

		List<Class<?>> params = Arrays.asList(method.getParameterTypes());
		return (proxy, args) -> marshalingCommunicator.callConstructor(instanceClass, params, argsToList(args));
	}

	private MethodHandler staticMethodHandler(Method method, MarshalingCommunicator<REF, TYPEREF, StudentSideException> marshalingCommunicator,
			String name)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> params = Arrays.asList(method.getParameterTypes());

		return (proxy, args) -> marshalingCommunicator.callStaticMethod(instanceClass, name, returnType, params, argsToList(args));
	}

	private MethodHandler staticFieldGetterHandler(Method method, MarshalingCommunicator<REF, TYPEREF, StudentSideException> marshalingCommunicator,
			String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters: " + method);

		return (proxy, args) -> marshalingCommunicator.getStaticField(instanceClass, name, returnType);
	}

	private MethodHandler staticFieldSetterHandler(Method method, MarshalingCommunicator<REF, TYPEREF, StudentSideException> marshalingCommunicator,
			String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + method);

		return staticFieldSetterHandlerChecked(marshalingCommunicator, name, paramTypes[0]);
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler staticFieldSetterHandlerChecked(MarshalingCommunicator<REF, TYPEREF, StudentSideException> marshalingCommunicator,
			String name, Class<F> fieldType)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			marshalingCommunicator.setStaticField(instanceClass, name, fieldType, argCasted);
			return null;
		};
	}

	private Class<?> chooseComponentTypeFromErasure(Class<?> componentTypeErasure, Method method)
	{
		return chooseComponentTypeFromErasure(componentTypeErasure, method, componentSSIType, componentSerializableType);
	}

	public static Class<?> chooseComponentTypeFromErasure(Class<?> componentTypeErasure, Method method, Class<?> componentSSIType, Class<?> componentSerializableType)
	{
		if(componentTypeErasure == StudentSideInstance.class)
			return componentSSIType;
		else if(componentTypeErasure == Object.class)
			return componentSerializableType;
		else
			throw new FrameworkCausedException("Component type erasure neither " + StudentSideInstance.class.getSimpleName()
					+ " nor " + Object.class.getSimpleName() + ": " + method);
	}

	private Class<?> getNthTypeArgumentOfPrototypeClassAndCheckIsClassOrNull(int argumentIndex)
	{
		return argumentIndex < 0 ? null : getNthTypeArgumentAndCheckIsClass(parameterizedPrototypeBaseClass, prototypeVariant.prototypeBaseClass(), argumentIndex);
	}

	private Class<?> getNthTypeArgumentAndCheckIsClass(ParameterizedType parameterizedType, Class<?> rawType, int typeArgumentIndex)
	{
		Type typeArgumentUnchecked = parameterizedType.getActualTypeArguments()[typeArgumentIndex];
		if(!(typeArgumentUnchecked instanceof Class<?> instanceType))
			throw new InconsistentHierarchyException("The type argument " + rawType.getTypeParameters()[typeArgumentIndex].getName()
					+ " to " + rawType.getSimpleName()
					+ " has to be an unparameterized or raw class: " + prototypeClass);

		return instanceType;
	}

	public static enum PrototypeVariant
	{
		NONARRAY(false, StudentSidePrototype.class, 0, -1, -1, -1),
		ARRAY_SSI(true, StudentSideArrayOfSSI.Prototype.class, 1, 0, -1, -1),
		ARRAY_SERIALIZABLE(true, StudentSideArrayOfSerializable.Prototype.class, 2, -1, 0, 1),
		ARRAY_SERIALIZBLE_SSI(true, StudentSideArrayOfSerializableSSI.Prototype.class, 3, 0, 1, 2);

		private final boolean	isArray;
		private final Class<?>	prototypeBaseClass;
		private final Class<?>	instanceBaseClass;

		private final int	arrayTypeArgumentIndex;
		private final int	ssiComponentTypeArgumentIndex;
		private final int	serializableComponentTypeArgumentIndex;
		private final int	arrayElementSerdesArgumentIndex;

		private PrototypeVariant(boolean isArray, Class<?> prototypeBaseClass,
				int arrayTypeArgumentIndex, int ssiComponentTypeArgumentIndex, int serializableComponentTypeArgumentIndex, int arrayElementSerdesArgumentIndex)
		{
			this.isArray = isArray;
			this.prototypeBaseClass = prototypeBaseClass;
			// All array-type prototypes are declared inside their corresponding SSI classes.
			Class<?> prototypeEnclosingClass = prototypeBaseClass.getEnclosingClass();
			this.instanceBaseClass = prototypeEnclosingClass == null ? StudentSideInstance.class : prototypeEnclosingClass;
			this.arrayTypeArgumentIndex = arrayTypeArgumentIndex;
			this.ssiComponentTypeArgumentIndex = ssiComponentTypeArgumentIndex;
			this.serializableComponentTypeArgumentIndex = serializableComponentTypeArgumentIndex;
			this.arrayElementSerdesArgumentIndex = arrayElementSerdesArgumentIndex;
		}

		public boolean isArray()
		{
			return isArray;
		}
		public Class<?> prototypeBaseClass()
		{
			return prototypeBaseClass;
		}
		public Class<?> instanceBaseClass()
		{
			return instanceBaseClass;
		}
		public int ssiComponentTypeArgumentIndex()
		{
			return ssiComponentTypeArgumentIndex;
		}
		public int serializableComponentTypeArgumentIndex()
		{
			return serializableComponentTypeArgumentIndex;
		}
		public int arrayTypeArgumentIndex()
		{
			return arrayTypeArgumentIndex;
		}
		public int arrayElementSerdesArgumentIndex()
		{
			return arrayElementSerdesArgumentIndex;
		}
	}
}
