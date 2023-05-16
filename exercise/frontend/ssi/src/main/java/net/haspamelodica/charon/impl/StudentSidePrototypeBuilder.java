package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkReturnAndParameterTypes;
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
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;

public final class StudentSidePrototypeBuilder<REF, TYPEREF extends REF, SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
{
	public final Class<SP>	prototypeClass;
	public final Class<SI>	instanceClass;

	public final MarshalingCommunicator<REF, TYPEREF, StudentSideException> prototypeWideMarshalingCommunicator;

	public final StudentSideTypeImpl<REF, TYPEREF> studentSideType;

	public final StudentSideInstanceBuilder<REF, TYPEREF, SI> instanceBuilder;

	private final Map<Method, MethodHandler> methodHandlers;

	public final SP prototype;

	public StudentSidePrototypeBuilder(MarshalingCommunicator<REF, TYPEREF, StudentSideException> globalMarshalingCommunicator, Class<SP> prototypeClass)
	{
		// The order of the following operations is important: each step depends on the last
		this.prototypeClass = prototypeClass;
		this.instanceClass = checkPrototypeClassAndGetInstanceClass();
		this.prototypeWideMarshalingCommunicator = createPrototypeWideMarshalingCommunicator(globalMarshalingCommunicator);
		this.studentSideType = lookupAndVerifyStudentSideType();
		this.instanceBuilder = new StudentSideInstanceBuilder<>(this);
		this.methodHandlers = createMethodHandlers();
		this.prototype = createPrototype();
	}

	private Class<SI> checkPrototypeClassAndGetInstanceClass()
	{
		if(!prototypeClass.isInterface())
			throw new InconsistentHierarchyException("Prototype classes have to be interfaces: " + prototypeClass);

		checkNotAnnotatedWith(prototypeClass, OverrideStudentSideName.class);
		checkNotAnnotatedWith(prototypeClass, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSideInstanceMethodKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSidePrototypeMethodKind.class);

		for(Type genericSuperinterface : prototypeClass.getGenericInterfaces())
			if(genericSuperinterface.equals(StudentSidePrototype.class))
				throw new InconsistentHierarchyException("A prototype class has to give a type argument to StudentClassPrototype: " + prototypeClass);
			else if(genericSuperinterface instanceof ParameterizedType parameterizedSuperinterface)
				if(parameterizedSuperinterface.getRawType() == StudentSidePrototype.class)
				{
					Type instanceTypeUnchecked = parameterizedSuperinterface.getActualTypeArguments()[0];
					if(!(instanceTypeUnchecked instanceof Class))
						throw new InconsistentHierarchyException("The type argument to StudentClassPrototype has to be an unparameterized or raw class: " + prototypeClass);

					// From the class type signature, we know SP's type parameter to StudentSidePrototype is SI.
					// So, this cast has to succeed.
					@SuppressWarnings("unchecked")
					Class<SI> instanceClass = (Class<SI>) instanceTypeUnchecked;
					return instanceClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement "
				+ StudentSidePrototype.class.getSimpleName() + " directly: " + prototypeClass);
	}

	private MarshalingCommunicator<REF, TYPEREF, StudentSideException> createPrototypeWideMarshalingCommunicator(
			MarshalingCommunicator<REF, TYPEREF, StudentSideException> globalMarshalingCommunicator)
	{
		return globalMarshalingCommunicator
				.withAdditionalSerDeses(getSerDeses(prototypeClass))
				.withAdditionalSerDeses(getSerDeses(instanceClass));
	}

	private StudentSideTypeImpl<REF, TYPEREF> lookupAndVerifyStudentSideType()
	{
		String studentSideName = getStudentSideName(instanceClass);
		TYPEREF typeref = prototypeWideMarshalingCommunicator.getTypeByName(studentSideName);

		StudentSideTypeImpl<REF, TYPEREF> studentSideType = new StudentSideTypeImpl<>(prototypeWideMarshalingCommunicator, typeref);
		if(!studentSideType.name().equals(studentSideName))
			throw new FrameworkCausedException("Name of type created by name mismatched: expected " + studentSideName + ", but was " + studentSideType.name());

		return studentSideType;
	}

	private record MethodWithHandler(Method method, MethodHandler handler)
	{}
	private Map<Method, MethodHandler> createMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods with getMethods(): abstract interface methods have to be public
		return Stream.concat(
				Arrays.stream(prototypeClass.getMethods())
						.map(m -> new MethodWithHandler(m, methodHandlerFor(m))),
				objectMethodHandlers())
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

	private SP createPrototype()
	{
		return createProxyInstance(prototypeClass, (proxy, method, args) -> methodHandlers.get(method).invoke(proxy, args));
	}

	private MethodHandler methodHandlerFor(Method method)
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
}
