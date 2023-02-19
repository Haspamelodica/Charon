package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.argsToList;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.createProxyInstance;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.defaultInstanceHandler;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.mapToStudentSide;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
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
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.marshaling.Marshaler;

// TODO type bound is wrong: StudentSideInstance only for forward refs
public final class StudentSideInstanceBuilder<REF, SI extends StudentSideInstance>
{
	public final StudentSideCommunicatorClientSide<REF>	communicator;
	public final Class<SI>								instanceClass;
	public final String									studentSideCN;

	public final Marshaler<REF> instanceWideMarshaler;

	private final Map<Method, InstanceMethodHandler<REF>> methodHandlers;

	public <SP extends StudentSidePrototype<SI>> StudentSideInstanceBuilder(StudentSidePrototypeBuilder<REF, SI, SP> prototypeBuilder)
	{
		this.communicator = prototypeBuilder.communicator;
		this.instanceClass = prototypeBuilder.instanceClass;
		this.studentSideCN = prototypeBuilder.studentSideCN;

		this.instanceWideMarshaler = prototypeBuilder.prototypeWideMarshaler;

		checkInstanceClass();
		this.methodHandlers = createMethodHandlers();
	}

	/**
	 * The returned object is guaranteed to be a proxy class (see {@link Proxy})
	 * whose invocation handler is a {@link StudentSideInstanceInvocationHandler}.
	 */
	public SI createInstance(REF ref)
	{
		return createProxyInstance(instanceClass, new StudentSideInstanceInvocationHandler<>(methodHandlers, ref));
	}

	private void checkInstanceClass()
	{
		checkNotAnnotatedWith(instanceClass, StudentSideInstanceMethodKind.class);
		checkNotAnnotatedWith(instanceClass, StudentSidePrototypeMethodKind.class);

		StudentSideInstanceKind kind = instanceClass.getAnnotation(StudentSideInstanceKind.class);
		if(kind == null)
			throw new InconsistentHierarchyException("A student-side instance class has to be annotated with StudentSideInstanceKind: " + instanceClass);
		if(kind.value() != Kind.CLASS)
			throw new FrameworkCausedException("Student-side interfaces aren't implemented yet");
	}

	private record MethodWithHandler<REF>(Method method, InstanceMethodHandler<REF> handler)
	{}
	private Map<Method, InstanceMethodHandler<REF>> createMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods with getMethods(): abstract interface methods have to be public
		return Stream.concat(
				Arrays.stream(instanceClass.getMethods())
						.map(m -> new MethodWithHandler<>(m, methodHandlerFor(m))),
				objectMethodHandlers())
				.collect(Collectors.toUnmodifiableMap(MethodWithHandler::method, MethodWithHandler::handler));
	}
	private Stream<MethodWithHandler<REF>> objectMethodHandlers()
	{
		// Yes, those methods could be overloaded. But even so, we wouldn't know what to do with the overloaded variants.
		// So, throwing an exception (via checkReturnAndParameterTypes) is appropriate.
		return Arrays.stream(Object.class.getMethods())
				.filter(method -> !Modifier.isFinal(method.getModifiers()))
				.map(method -> switch(method.getName())
				{
					case "toString" -> new MethodWithHandler<>(checkReturnAndParameterTypes(method, String.class),
							(ref, proxy, args) -> "StudentSideInstance[" + ref + "]");
					case "hashCode" -> new MethodWithHandler<>(checkReturnAndParameterTypes(method, int.class),
							(ref, proxy, args) -> System.identityHashCode(proxy));
					case "equals" -> new MethodWithHandler<>(checkReturnAndParameterTypes(method, boolean.class, Object.class),
							(ref, proxy, args) -> proxy == args[0]);
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

	private InstanceMethodHandler<REF> methodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
		Marshaler<REF> methodWideMarshaler = instanceWideMarshaler.withAdditionalSerDeses(getSerDeses(method));

		InstanceMethodHandler<REF> defaultHandler = defaultInstanceHandler(method);
		return handlerFor(method, StudentSideInstanceMethodKind.class, defaultHandler,
				(kind, name, nameOverridden) -> switch(kind.value())
				{
					case INSTANCE_METHOD -> methodHandler(methodWideMarshaler, method, name);
					case INSTANCE_FIELD_GETTER -> fieldGetterHandler(methodWideMarshaler, method, name);
					case INSTANCE_FIELD_SETTER -> fieldSetterHandler(methodWideMarshaler, method, name);
				});
	}

	private InstanceMethodHandler<REF> methodHandler(Marshaler<REF> marshaler, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> paramTypes = Arrays.asList(method.getParameterTypes());

		String returnCN = mapToStudentSide(returnType);
		List<String> paramCNs = mapToStudentSide(paramTypes);

		return (ref, proxy, args) ->
		{
			List<REF> argRefs = marshaler.send(paramTypes, argsToList(args));
			REF resultRef = communicator.callInstanceMethod(studentSideCN, name, returnCN, paramCNs, ref, argRefs);
			return marshaler.receive(returnType, resultRef);
		};
	}

	private InstanceMethodHandler<REF> fieldGetterHandler(Marshaler<REF> marshaler, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		String returnCN = mapToStudentSide(returnType);

		return (ref, proxy, args) ->
		{
			REF resultRef = communicator.getInstanceField(studentSideCN, name, returnCN, ref);
			return marshaler.receive(returnType, resultRef);
		};
	}

	private InstanceMethodHandler<REF> fieldSetterHandler(Marshaler<REF> marshaler, Method method, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		Class<?> paramType = paramTypes[0];

		return fieldSetterHandlerChecked(marshaler, name, paramType);
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> InstanceMethodHandler<REF> fieldSetterHandlerChecked(Marshaler<REF> marshaler, String name, Class<F> fieldType)
	{
		String fieldCN = mapToStudentSide(fieldType);

		return (ref, proxy, args) ->
		{
			@SuppressWarnings("unchecked") // We could
			F argCasted = (F) args[0];
			REF valRef = marshaler.send(fieldType, argCasted);
			communicator.setInstanceField(studentSideCN, name, fieldCN, ref, valRef);
			return null;
		};
	}
}
