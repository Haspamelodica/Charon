package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkReturnAndParameterTypes;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getStudentSideName;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideComponentTypeByClass;
import net.haspamelodica.charon.annotations.StudentSideComponentTypeByName;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.util.LazyValue;

public final class StudentSideInstanceBuilder<REF, TYPEREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		SI extends StudentSideInstance>
{
	public final Class<SI>																			instanceClass;
	public final MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException>	instanceWideMarshalingCommunicator;
	public final StudentSideInstanceKind.Kind														kind;

	public final TYPEREF							studentSideComponentType;
	public final StudentSideTypeImpl<REF, TYPEREF>	studentSideType;

	private final LazyValue<Map<Method, MethodHandler>> methodHandlers;

	public <SP extends StudentSidePrototype<SI>> StudentSideInstanceBuilder(
			StudentSidePrototypeBuilder<REF, TYPEREF, ?, METHODREF, FIELDREF, SI, SP> prototypeBuilder)
	{
		this.instanceClass = prototypeBuilder.instanceClass;
		this.instanceWideMarshalingCommunicator = prototypeBuilder.prototypeWideMarshalingCommunicator;

		this.kind = checkInstanceClassAndGetInstanceKind();
		this.studentSideComponentType = lookupAndVerifyStudentSideComponentType();
		this.studentSideType = lookupAndVerifyStudentSideType();
		this.methodHandlers = new LazyValue<>(this::createMethodHandlers);
	}

	public SI createInstance(REF ref)
	{
		return createProxyInstance(instanceClass, new InvocationHandler()
		{
			// This field is needed to avoid GC throwing away the ref until the SSI is unreachable.
			//TODO if we store the ref either way, optimize away RefTranslator's backwardRefs map.
			// The same goes for callbacks in the student side
			@SuppressWarnings("unused")
			private final Object refField = ref;

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
			{
				return methodHandlers.get().get(method).invoke(proxy, args);
			}
		});
	}

	private StudentSideInstanceKind.Kind checkInstanceClassAndGetInstanceKind()
	{
		checkNotAnnotatedWith(instanceClass, StudentSideInstanceMethodKind.class);
		checkNotAnnotatedWith(instanceClass, StudentSidePrototypeMethodKind.class);

		StudentSideInstanceKind kindAnnotation = instanceClass.getAnnotation(StudentSideInstanceKind.class);
		if(kindAnnotation == null)
			throw new InconsistentHierarchyException("A student-side instance class has to be annotated with StudentSideInstanceKind: " + instanceClass);

		return kindAnnotation.value();
	}

	private TYPEREF lookupAndVerifyStudentSideComponentType()
	{
		if(kind != Kind.ARRAY)
		{
			checkNotAnnotatedWith(instanceClass, StudentSideComponentTypeByClass.class);
			checkNotAnnotatedWith(instanceClass, StudentSideComponentTypeByName.class);
			return null;
		}

		StudentSideComponentTypeByClass componentTypeClass = instanceClass.getAnnotation(StudentSideComponentTypeByClass.class);
		if(componentTypeClass != null)
		{
			checkNotAnnotatedWith(instanceClass, StudentSideComponentTypeByName.class);
			return instanceWideMarshalingCommunicator.lookupCorrespondingStudentSideTypeOrThrow(componentTypeClass.value());
		}

		StudentSideComponentTypeByName componentTypeName = instanceClass.getAnnotation(StudentSideComponentTypeByName.class);
		if(componentTypeName != null)
			return instanceWideMarshalingCommunicator.getTypeByNameAndVerify(componentTypeName.value());

		throw new InconsistentHierarchyException("Array instance class has to be annotated with either "
				+ StudentSideComponentTypeByClass.class.getSimpleName() + " or "
				+ StudentSideComponentTypeByName.class.getSimpleName() + ": " + instanceClass);
	}

	private StudentSideTypeImpl<REF, TYPEREF> lookupAndVerifyStudentSideType()
	{
		return new StudentSideTypeImpl<>(instanceWideMarshalingCommunicator,
				kind == Kind.ARRAY ? lookupAndVerifyTyperefForArray() : lookupAndVerifyTyperefForNonarray());
	}

	private TYPEREF lookupAndVerifyTyperefForArray()
	{
		return instanceWideMarshalingCommunicator.getArrayType(studentSideComponentType);
	}

	private TYPEREF lookupAndVerifyTyperefForNonarray()
	{
		return instanceWideMarshalingCommunicator.getTypeByNameAndVerify(getStudentSideName(instanceClass));
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
		return Arrays.stream(Object.class.getMethods())
				.filter(method -> !Modifier.isFinal(method.getModifiers()))
				.map(method -> switch(method.getName())
				{
					case "toString" -> new MethodWithHandler(checkReturnAndParameterTypes(method, String.class),
							(proxy, args) -> "SSI" + instanceWideMarshalingCommunicator.getRawRef(proxy));
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
		return Arrays.stream(instanceClass.getMethods()).map(m -> new MethodWithHandler(m, ssiMethodHandlerFor(m)));
	}

	private MethodHandler ssiMethodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
		checkNotAnnotatedWith(method, PrototypeClass.class);
		MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> methodWideMarshalingCommunicator =
				instanceWideMarshalingCommunicator.withAdditionalSerDeses(getSerDeses(method));

		return handlerFor(method, StudentSideInstanceMethodKind.class,
				(kindAnnotation, name, nameOverridden) ->
				{
					StudentSideInstanceMethodKind.Kind kind = kindAnnotation.value();
					if(!kind.allowedInstanceKinds().contains(this.kind))
						throw new InconsistentHierarchyException("An instance method of kind " + kind
								+ " isn't allowed for an SSI of kind " + this.kind + ": " + instanceClass);
					return switch(kind)
					{
						case INSTANCE_METHOD -> instanceMethodHandler(methodWideMarshalingCommunicator, method, name);
						case INSTANCE_FIELD_GETTER -> instanceFieldGetterHandler(methodWideMarshalingCommunicator, method, name);
						case INSTANCE_FIELD_SETTER -> instanceFieldSetterHandler(methodWideMarshalingCommunicator, method, name);
						case ARRAY_LENGTH -> arrayLengthHandler(method, methodWideMarshalingCommunicator, nameOverridden);
						case ARRAY_GETTER -> arrayGetterHandler(method, methodWideMarshalingCommunicator, nameOverridden);
						case ARRAY_SETTER -> arraySetterHandler(method, methodWideMarshalingCommunicator, nameOverridden);
						case SERIALIZATION_RECEIVER -> serializationReceiverHandler(method, methodWideMarshalingCommunicator, nameOverridden);
					};
				});
	}

	private MethodHandler instanceMethodHandler(
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> params = Arrays.asList(method.getParameterTypes());

		METHODREF methodref = marshalingCommunicator.lookupMethod(instanceClass, name, returnType, params, false);
		return (proxy, args) -> marshalingCommunicator.callInstanceMethod(methodref, instanceClass, returnType, params,
				proxy, argsToList(args));
	}

	private MethodHandler instanceFieldGetterHandler(
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			Method method, String name)
	{
		Class<?> localReturnType = method.getReturnType();
		if(localReturnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		Class<?> fieldType = localReturnType;

		FIELDREF fieldref = marshalingCommunicator.lookupField(instanceClass, name, fieldType, false);
		return (proxy, args) -> marshalingCommunicator.getInstanceField(fieldref, instanceClass, fieldType, proxy);
	}

	private MethodHandler instanceFieldSetterHandler(
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			Method method, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		return fieldSetterHandlerChecked(marshalingCommunicator, name, paramTypes[0]);
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler fieldSetterHandlerChecked(
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			String name, Class<F> fieldType)
	{
		FIELDREF fieldref = marshalingCommunicator.lookupField(instanceClass, name, fieldType, false);
		return (proxy, args) ->
		{
			// We could use fieldType to actually check this,
			// but if something goes wrong here, it's the exercise creator's fault (or of Java Proxy classes)
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			marshalingCommunicator.setInstanceField(fieldref, instanceClass, fieldType, proxy, argCasted);
			return null;
		};
	}

	private MethodHandler arrayLengthHandler(Method method,
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			boolean nameOverridden)
	{
		if(nameOverridden)
			throw new InconsistentHierarchyException("Array length method had name overridden: " + method);

		if(method.getParameterCount() != 0)
			throw new InconsistentHierarchyException("Array length method had parameters: " + method);

		if(method.getReturnType() != int.class)
			throw new InconsistentHierarchyException("Array length method's return type wasn't int: " + method);

		return (proxy, args) -> instanceWideMarshalingCommunicator.getArrayLength(instanceClass, proxy);
	}

	private MethodHandler arrayGetterHandler(Method method,
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			boolean nameOverridden)
	{
		if(nameOverridden)
			throw new InconsistentHierarchyException("Array getter had name overridden: " + method);

		if(method.getParameterCount() != 1)
			throw new InconsistentHierarchyException("Array getter had not exactly one parameter: " + method);

		if(method.getParameters()[0].getType() != int.class)
			throw new InconsistentHierarchyException("Array getter's parameter wasn't int: " + method);

		Class<?> valueType = method.getReturnType();
		TYPEREF studentSideValueType = marshalingCommunicator.lookupCorrespondingStudentSideTypeOrThrow(valueType);
		if(studentSideValueType != studentSideComponentType)
			throw new InconsistentHierarchyException("Unexpected student side type corresponding to array getter's return type; expected "
					+ marshalingCommunicator.describeType(studentSideComponentType).name() + ", but was "
					+ marshalingCommunicator.describeType(studentSideValueType).name() + ": " + method);

		return (proxy, args) -> instanceWideMarshalingCommunicator.getArrayElement(instanceClass, valueType, proxy, (Integer) args[0]);
	}

	private MethodHandler arraySetterHandler(Method method,
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			boolean nameOverridden)
	{
		if(nameOverridden)
			throw new InconsistentHierarchyException("Array setter had name overridden: " + method);

		if(method.getParameterCount() != 2)
			throw new InconsistentHierarchyException("Array setter had not exactly two parameters: " + method);

		if(method.getReturnType() != void.class)
			throw new InconsistentHierarchyException("Array setter's return type wasn't void: " + method);

		Parameter[] parameters = method.getParameters();

		if(parameters[0].getType() != int.class)
			throw new InconsistentHierarchyException("Array setter's first parameter wasn't int: " + method);

		Class<?> valueType = parameters[1].getType();
		TYPEREF studentSideValueType = marshalingCommunicator.lookupCorrespondingStudentSideTypeOrThrow(valueType);
		if(studentSideValueType != studentSideComponentType)
			throw new InconsistentHierarchyException("Unexpected student side type corresponding to array getter's second parameter; expected "
					+ marshalingCommunicator.describeType(studentSideComponentType).name() + ", but was "
					+ marshalingCommunicator.describeType(studentSideValueType).name() + ": " + method);

		return (proxy, args) ->
		{
			instanceWideMarshalingCommunicator.setArrayElement(instanceClass, valueType, proxy, (Integer) args[0], args[1]);
			return null;
		};
	}

	private MethodHandler serializationReceiverHandler(Method method,
			MarshalingCommunicator<REF, TYPEREF, ?, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			boolean nameOverridden)
	{
		if(nameOverridden)
			throw new InconsistentHierarchyException("Serialization receiver method had name overridden: " + method);

		Parameter[] parameters = method.getParameters();
		if(parameters.length != 0)
			throw new InconsistentHierarchyException("Serialization receiver method had parameters: " + method);

		Class<?> returnType = method.getReturnType();

		TYPEREF studentSideReturnType = marshalingCommunicator.lookupCorrespondingStudentSideTypeOrThrow(returnType);
		// Unfortunately, we can only do this check at generation time if there are no SerDeses given as parameters
		if(studentSideReturnType != studentSideType.getTyperef())
			throw new InconsistentHierarchyException("Serialization receiver method has incorrect return type: " + method);

		return (proxy, args) -> marshalingCommunicator.sendAndReceive(returnType, proxy);
	}
}
