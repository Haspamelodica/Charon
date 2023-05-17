package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkReturnAndParameterTypes;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.defaultHandler;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;

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
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.impl.StudentSidePrototypeBuilder.PrototypeVariant;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSSI;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSerializable;
import net.haspamelodica.charon.studentsideinstances.StudentSideArrayOfSerializableSSI;

public final class StudentSideInstanceBuilder<REF, TYPEREF extends REF, SI extends StudentSideInstance>
{
	public final Class<SI>						instanceClass;
	public final StudentSideInstanceKind.Kind	kind;

	private final PrototypeVariant	prototypeVariant;
	private final Class<?>			componentSSIType;
	private final Class<?>			componentSerializableType;

	public final MarshalingCommunicator<REF, TYPEREF, StudentSideException> instanceWideMarshalingCommunicator;

	private final Map<Method, MethodHandler> methodHandlers;

	public <SP extends StudentSidePrototype<SI>> StudentSideInstanceBuilder(StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilder)
	{
		this.instanceClass = prototypeBuilder.instanceClass;
		this.prototypeVariant = prototypeBuilder.prototypeVariant;
		this.componentSSIType = prototypeBuilder.componentSSIType;
		this.componentSerializableType = prototypeBuilder.componentSerializableType;

		this.instanceWideMarshalingCommunicator = prototypeBuilder.prototypeWideMarshalingCommunicator;

		this.kind = checkInstanceClassAndGetInstanceKind();
		this.methodHandlers = createMethodHandlers();
	}

	public SI createInstance()
	{
		return createProxyInstance(instanceClass, (proxy, method, args) -> methodHandlers.get(method).invoke(proxy, args));
	}

	private StudentSideInstanceKind.Kind checkInstanceClassAndGetInstanceKind()
	{
		checkNotAnnotatedWith(instanceClass, StudentSideInstanceMethodKind.class);
		checkNotAnnotatedWith(instanceClass, StudentSidePrototypeMethodKind.class);

		checkInstanceClassDoesNotImplementExceptIfArrayVariantMatches(StudentSideArrayOfSSI.class);
		checkInstanceClassDoesNotImplementExceptIfArrayVariantMatches(StudentSideArrayOfSerializable.class);
		checkInstanceClassDoesNotImplementExceptIfArrayVariantMatches(StudentSideArrayOfSerializableSSI.class);

		StudentSideInstanceKind kindAnnotation = instanceClass.getAnnotation(StudentSideInstanceKind.class);
		if(kindAnnotation == null)
			throw new InconsistentHierarchyException("A student-side instance class has to be annotated with StudentSideInstanceKind: " + instanceClass);

		StudentSideInstanceKind.Kind kind = kindAnnotation.value();
		if(prototypeVariant.isArray())
		{
			if(kind != StudentSideInstanceKind.Kind.ARRAY)
				throw new InconsistentHierarchyException("A student-side instance class for an array prototype has to be declared to have kind ARRAY, but was "
						+ kind + ": " + instanceClass);
		} else if(kind != StudentSideInstanceKind.Kind.CLASS && kind != StudentSideInstanceKind.Kind.INTERFACE)
			throw new InconsistentHierarchyException("A student-side instance class for a non-array prototype can't be declared to have kind "
					+ kind + ": " + instanceClass);

		return kind;
	}

	private void checkInstanceClassDoesNotImplementExceptIfArrayVariantMatches(Class<?> clazz)
	{
		if(clazz != prototypeVariant.instanceBaseClass() && clazz.isAssignableFrom(instanceClass))
			throw new InconsistentHierarchyException("A student-side instance class unexpectedly implemented "
					+ clazz.getSimpleName() + ": " + instanceClass);
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
							(proxy, args) -> "SSI#" + Integer.toHexString(System.identityHashCode(proxy))); //TODO can we easily show the RefID here?
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
		return Arrays.stream(instanceClass.getMethods())
				.map(prototypeVariant.isArray() ? this::arrayMethodHandlerFor : m -> new MethodWithHandler(m, ssiMethodHandlerFor(m)));
	}

	private MethodWithHandler arrayMethodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);

		if(!Modifier.isAbstract(method.getModifiers()))
		{
			checkNotAnnotatedWith(method, StudentSideInstanceMethodKind.class);
			return new MethodWithHandler(method, defaultHandler(method));
		}

		if(method.getDeclaringClass() != prototypeVariant.instanceBaseClass())
			throw new InconsistentHierarchyException("An array SSI can't define new abstract methods: " + instanceClass);

		return switch(method.getName())
		{
			case "length" -> new MethodWithHandler(checkReturnAndParameterTypes(method, int.class),
					arrayLengthHandler());
			case "get", "getAsSerializable" -> new MethodWithHandler(method, arrayGetMethodHandler(method));
			case "set" -> new MethodWithHandler(method, arraySetMethodHandler(method));
			default -> throw new FrameworkCausedException("Unknown method of " + prototypeVariant.instanceBaseClass() + ": " + method);
		};
	}

	private MethodHandler arrayLengthHandler()
	{
		return (proxy, args) -> instanceWideMarshalingCommunicator.getArrayLength(instanceClass, proxy);
	}

	private MethodHandler arrayGetMethodHandler(Method method)
	{
		if(method.getParameterCount() != 1)
			throw new FrameworkCausedException("Array get method had not exactly one parameter");

		if(method.getParameters()[0].getType() != int.class)
			throw new FrameworkCausedException("Array get method's parameter wasn't int");

		Class<?> valueType = chooseComponentTypeFromErasure(method.getReturnType(), method);

		return (proxy, args) -> instanceWideMarshalingCommunicator.getArrayElement(instanceClass, valueType, proxy, (Integer) args[0]);
	}

	private MethodHandler arraySetMethodHandler(Method method)
	{
		if(method.getParameterCount() != 2)
			throw new FrameworkCausedException("Array set method had not exactly two parameters");

		if(method.getReturnType() != void.class)
			throw new FrameworkCausedException("Array set method's return type wasn't void");

		Parameter[] parameters = method.getParameters();

		if(parameters[0].getType() != int.class)
			throw new FrameworkCausedException("Array set method's first parameter wasn't int");

		Class<?> valueType = chooseComponentTypeFromErasure(parameters[1].getType(), method);

		return (proxy, args) ->
		{
			instanceWideMarshalingCommunicator.setArrayElement(instanceClass, valueType, proxy, (Integer) args[0], args[1]);
			return null;
		};
	}

	private MethodHandler ssiMethodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
		MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator =
				instanceWideMarshalingCommunicator.withAdditionalSerDeses(getSerDeses(method));

		return handlerFor(method, StudentSideInstanceMethodKind.class,
				(kind, name, nameOverridden) -> switch(kind.value())
				{
					case INSTANCE_METHOD -> instanceMethodHandler(methodWideMarshalingCommunicator, method, name);
					case INSTANCE_FIELD_GETTER -> instanceFieldGetterHandler(methodWideMarshalingCommunicator, method, name);
					case INSTANCE_FIELD_SETTER -> instanceFieldSetterHandler(methodWideMarshalingCommunicator, method, name);
				});
	}

	private MethodHandler instanceMethodHandler(MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator,
			Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> params = Arrays.asList(method.getParameterTypes());

		return (proxy, args) -> methodWideMarshalingCommunicator.callInstanceMethod(instanceClass, name, returnType, params,
				proxy, argsToList(args));
	}

	private MethodHandler instanceFieldGetterHandler(MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator,
			Method method, String name)
	{
		if(kind != Kind.CLASS)
			throw new InconsistentHierarchyException("Only student-side classes can have field getters: " + method);

		Class<?> localReturnType = method.getReturnType();
		if(localReturnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		Class<?> fieldType = localReturnType;

		return (proxy, args) -> methodWideMarshalingCommunicator.getInstanceField(instanceClass, name, fieldType, proxy);
	}

	private MethodHandler instanceFieldSetterHandler(MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator,
			Method method, String name)
	{
		if(kind != Kind.CLASS)
			throw new InconsistentHierarchyException("Only student-side classes can have field setters: " + method);

		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		return fieldSetterHandlerChecked(methodWideMarshalingCommunicator, name, paramTypes[0]);
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler fieldSetterHandlerChecked(MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator,
			String name, Class<F> fieldType)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked") // We could
			F argCasted = (F) args[0];
			methodWideMarshalingCommunicator.setInstanceField(instanceClass, name, fieldType, proxy, argCasted);
			return null;
		};
	}

	private Class<?> chooseComponentTypeFromErasure(Class<?> componentTypeErasure, Method method)
	{
		return StudentSidePrototypeBuilder.chooseComponentTypeFromErasure(componentTypeErasure, method, componentSSIType, componentSerializableType);
	}
}
