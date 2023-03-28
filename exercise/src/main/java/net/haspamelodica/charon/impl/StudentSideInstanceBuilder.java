package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.mapToStudentSide;
import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import net.haspamelodica.charon.impl.StudentSideImplUtils.StudentSideType;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;

// TODO type bound is wrong: StudentSideInstance only for forward refs
public final class StudentSideInstanceBuilder<SI extends StudentSideInstance>
{
	public final Class<SI>						instanceClass;
	public final StudentSideType<SI>			instanceStudentSideType;
	public final StudentSideInstanceKind.Kind	kind;

	public final MarshalingCommunicator<?> instanceWideMarshalingCommunicator;

	private final Map<Method, MethodHandler> methodHandlers;

	public <SP extends StudentSidePrototype<SI>> StudentSideInstanceBuilder(StudentSidePrototypeBuilder<SI, SP> prototypeBuilder)
	{
		this.instanceClass = prototypeBuilder.instanceClass;
		this.instanceStudentSideType = prototypeBuilder.instanceStudentSideType;

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

		StudentSideInstanceKind kind = instanceClass.getAnnotation(StudentSideInstanceKind.class);
		if(kind == null)
			throw new InconsistentHierarchyException("A student-side instance class has to be annotated with StudentSideInstanceKind: " + instanceClass);
		return kind.value();
	}

	private record MethodWithHandler(Method method, MethodHandler handler)
	{}
	private Map<Method, MethodHandler> createMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods with getMethods(): abstract interface methods have to be public
		return Stream.concat(
				Arrays.stream(instanceClass.getMethods())
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
							(proxy, args) -> "SSI#" + Integer.toHexString(System.identityHashCode(proxy))); //TODO can we easily show the RefID here?
					case "hashCode" -> new MethodWithHandler(checkReturnAndParameterTypes(method, int.class),
							(proxy, args) -> System.identityHashCode(proxy));
					case "equals" -> new MethodWithHandler(checkReturnAndParameterTypes(method, boolean.class, Object.class),
							(proxy, args) -> proxy == args[0]);
					default -> throw new FrameworkCausedException("Unknown method of Object: " + method);
				});
	}

	private Method checkReturnAndParameterTypes(Method method, Class<?> expectedReturnType, Class<?>... expectedParameterTypes)
	{
		if(!Arrays.equals(method.getParameterTypes(), expectedParameterTypes))
			throw new FrameworkCausedException("Unexpected parameter types: expected " + expectedParameterTypes + " for " + method);
		if(!method.getReturnType().equals(expectedReturnType))
			throw new FrameworkCausedException("Unknown method of Object: " + method);
		return method;
	}

	private MethodHandler methodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
		MarshalingCommunicator<?> methodWideMarshalingCommunicator = instanceWideMarshalingCommunicator.withAdditionalSerDeses(getSerDeses(method));

		return handlerFor(method, StudentSideInstanceMethodKind.class,
				(kind, name, nameOverridden) -> switch(kind.value())
				{
					case INSTANCE_METHOD -> instanceMethodHandler(methodWideMarshalingCommunicator, method, name);
					case INSTANCE_FIELD_GETTER -> instanceFieldGetterHandler(methodWideMarshalingCommunicator, method, name);
					case INSTANCE_FIELD_SETTER -> instanceFieldSetterHandler(methodWideMarshalingCommunicator, method, name);
				});
	}

	private MethodHandler instanceMethodHandler(MarshalingCommunicator<?> methodWideMarshalingCommunicator, Method method, String name)
	{
		StudentSideType<?> returnType = mapToStudentSide(method.getReturnType());
		List<StudentSideType<?>> params = mapToStudentSide(Arrays.asList(method.getParameterTypes()));

		return (proxy, args) -> methodWideMarshalingCommunicator.callInstanceMethod(instanceStudentSideType, name, returnType, params, proxy, argsToList(args));
	}

	private MethodHandler instanceFieldGetterHandler(MarshalingCommunicator<?> methodWideMarshalingCommunicator, Method method, String name)
	{
		if(kind != Kind.CLASS)
			throw new InconsistentHierarchyException("Only student-side classes can have field getters: " + method);

		Class<?> localReturnType = method.getReturnType();
		if(localReturnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		StudentSideType<?> fieldType = mapToStudentSide(localReturnType);

		return (proxy, args) -> methodWideMarshalingCommunicator.getInstanceField(instanceStudentSideType, name, fieldType, proxy);
	}

	private MethodHandler instanceFieldSetterHandler(MarshalingCommunicator<?> methodWideMarshalingCommunicator, Method method, String name)
	{
		if(kind != Kind.CLASS)
			throw new InconsistentHierarchyException("Only student-side classes can have field setters: " + method);

		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		return fieldSetterHandlerChecked(methodWideMarshalingCommunicator, name, mapToStudentSide(paramTypes[0]));
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler fieldSetterHandlerChecked(
			MarshalingCommunicator<?> methodWideMarshalingCommunicator, String name, StudentSideType<F> fieldType)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked") // We could
			F argCasted = (F) args[0];
			methodWideMarshalingCommunicator.setInstanceField(instanceStudentSideType, name, fieldType, proxy, argCasted);
			return null;
		};
	}
}
