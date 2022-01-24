package net.haspamelodica.studentcodeseparator.impl;

import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.argsToList;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.createProxyInstance;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.defaultHandler;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.getSerializers;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.c2n;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.studentcodeseparator.StudentSideObject;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind.ObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;

public final class StudentSideObjectBuilder<REF, SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
{
	public final StudentSideCommunicator<REF>	communicator;
	public final Class<SO>						objectClass;
	public final String							studentSideCN;

	public final SerializationHandler<REF> objectWideSerializer;

	private final Map<Method, Function<REF, MethodHandler>> methodHandlerGenerators;

	public StudentSideObjectBuilder(StudentSidePrototypeBuilder<REF, SO, SP> prototypeBuilder)
	{
		this.communicator = prototypeBuilder.communicator;
		this.objectClass = prototypeBuilder.objectClass;
		this.studentSideCN = prototypeBuilder.studentSideCN;

		this.objectWideSerializer = prototypeBuilder.prototypeWideSerializer;

		checkObjectClass();
		this.methodHandlerGenerators = createMethodHandlerGenerators();
	}

	public SO createInstance(REF ref)
	{
		Map<Method, MethodHandler> methodHandlers = methodHandlerGenerators
				.entrySet()
				.stream()
				.collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> e.getValue().apply(ref)));
		return createProxyInstance(objectClass, methodHandlers);
	}

	private void checkObjectClass()
	{
		checkNotAnnotatedWith(objectClass, StudentSideObjectMethodKind.class);
		checkNotAnnotatedWith(objectClass, StudentSidePrototypeMethodKind.class);

		StudentSideObjectKind kind = objectClass.getAnnotation(StudentSideObjectKind.class);
		if(kind == null)
			throw new InconsistentHierarchyException("A student-side object class has to be annotated with StudentSideObjectKind: " + objectClass);
		if(kind.value() != ObjectKind.CLASS)
			throw new IllegalArgumentException("Student-side interfaces aren't implemented yet");
	}

	private Map<Method, Function<REF, MethodHandler>> createMethodHandlerGenerators()
	{
		// We are guaranteed to catch all (relevant) methods this way: abstract interface methods have to be public
		return Arrays.stream(objectClass.getMethods())
				.collect(Collectors.toUnmodifiableMap(m -> m, this::methodHandlerGeneratorFor));
	}

	private Function<REF, MethodHandler> methodHandlerGeneratorFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideObjectKind.class);
		checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
		SerializationHandler<REF> serializerMethod = objectWideSerializer.withAdditionalSerializers(getSerializers(method));

		MethodHandler defaultHandler = defaultHandler(method);
		return handlerFor(method, StudentSideObjectMethodKind.class, ref -> defaultHandler,
				(kind, name, nameOverridden) -> switch(kind.value())
				{
				case INSTANCE_METHOD -> instanceMethodHandlerGenerator(studentSideCN, serializerMethod, method, name);
				case FIELD_GETTER -> fieldGetterHandlerGenerator(studentSideCN, serializerMethod, method, name);
				case FIELD_SETTER -> fieldSetterHandlerGenerator(studentSideCN, serializerMethod, method, name);
				});
	}

	private Function<REF, MethodHandler>
			instanceMethodHandlerGenerator(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> paramTypes = Arrays.asList(method.getParameterTypes());

		String returnCN = c2n(returnType);
		List<String> paramCNs = c2n(paramTypes);

		return ref -> (proxy, args) ->
		{
			List<REF> argRefs = serializer.send(paramTypes, argsToList(args));
			REF resultRef = communicator.callInstanceMethod(studentSideCN, name, returnCN, paramCNs, ref, argRefs);
			return serializer.receive(returnType, resultRef);
		};
	}

	private Function<REF, MethodHandler>
			fieldGetterHandlerGenerator(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		String returnCN = c2n(returnType);

		return ref -> (proxy, args) ->
		{
			REF resultRef = communicator.getField(studentSideCN, name, returnCN, ref);
			return serializer.receive(returnType, resultRef);
		};
	}

	private Function<REF, MethodHandler>
			fieldSetterHandlerGenerator(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		Class<?> paramType = paramTypes[0];

		return fieldSetterHandlerGeneratorChecked(studentSideCN, serializer, name, paramType);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F> Function<REF, MethodHandler>
			fieldSetterHandlerGeneratorChecked(String studentSideCN, SerializationHandler<REF> serializer, String name, Class<F> fieldType)
	{
		String fieldCN = c2n(fieldType);

		return ref -> (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			REF valRef = serializer.send(fieldType, argCasted);
			communicator.setField(studentSideCN, name, fieldCN, ref, valRef);
			return null;
		};
	}
}
