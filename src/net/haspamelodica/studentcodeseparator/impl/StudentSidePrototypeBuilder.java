package net.haspamelodica.studentcodeseparator.impl;

import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.argsToList;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.createProxyInstance;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.getName;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.getSerializers;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.c2n;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.haspamelodica.studentcodeseparator.StudentSideObject;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;
import net.haspamelodica.studentcodeseparator.serialization.SerializationHandler;

public final class StudentSidePrototypeBuilder<REF, SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
{
	public final StudentSideCommunicator<REF>	communicator;
	public final Class<SP>						prototypeClass;

	public final Class<SO>					objectClass;
	public final String						studentSideCN;
	public final SerializationHandler<REF>	prototypeWideSerializer;

	public final StudentSideObjectBuilder<REF, SO, SP> objectBuilder;

	private final Map<Method, MethodHandler> methodHandlers;

	public StudentSidePrototypeBuilder(StudentSideCommunicator<REF> communicator, Class<SP> prototypeClass)
	{
		this.communicator = communicator;
		this.prototypeClass = prototypeClass;

		//The order of the following operations is important: each step depends on the last

		this.objectClass = checkPrototypeClassAndGetObjectClass();
		this.studentSideCN = getName(objectClass);
		this.prototypeWideSerializer = createPrototypeWideSerializer();

		this.objectBuilder = new StudentSideObjectBuilder<>(this);

		this.methodHandlers = createMethodHandlers();
	}

	public SP createPrototype()
	{
		return createProxyInstance(prototypeClass, methodHandlers);
	}

	private Class<SO> checkPrototypeClassAndGetObjectClass()
	{
		if(!prototypeClass.isInterface())
			throw new InconsistentHierarchyException("Prototype classes have to be interfaces: " + prototypeClass);

		checkNotAnnotatedWith(prototypeClass, StudentSideObjectKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSideObjectMethodKind.class);
		checkNotAnnotatedWith(prototypeClass, StudentSidePrototypeMethodKind.class);

		for(Type genericSuperinterface : prototypeClass.getGenericInterfaces())
			if(genericSuperinterface.equals(StudentSidePrototype.class))
				throw new InconsistentHierarchyException("A prototype class has to give a type argument to StudentClassPrototype: " + prototypeClass);
			else if(genericSuperinterface instanceof ParameterizedType parameterizedSuperinterface)
				if(parameterizedSuperinterface.getRawType() == StudentSidePrototype.class)
				{
					Type objectTypeUnchecked = parameterizedSuperinterface.getActualTypeArguments()[0];
					if(!(objectTypeUnchecked instanceof Class))
						throw new InconsistentHierarchyException("The type argument to StudentClassPrototype has to be an unparameterized or raw class: " + prototypeClass);

					//From the class type signature, we know SP's type parameter to StudentSidePrototype is SO.
					//So, this cast has to succeed.
					@SuppressWarnings("unchecked")
					Class<SO> objectClass = (Class<SO>) objectTypeUnchecked;
					return objectClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement StudentClassPrototype directly: " + prototypeClass);
	}

	private SerializationHandler<REF> createPrototypeWideSerializer()
	{
		return new SerializationHandler<>(communicator)
				.withAdditionalSerializers(getSerializers(prototypeClass))
				.withAdditionalSerializers(getSerializers(objectClass));
	}

	private Map<Method, MethodHandler> createMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods this way: abstract interface methods have to be public
		return Arrays.stream(prototypeClass.getMethods())
				.collect(Collectors.toUnmodifiableMap(m -> m, this::methodHandlerFor));
	}

	private MethodHandler methodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideObjectKind.class);
		checkNotAnnotatedWith(method, StudentSideObjectMethodKind.class);
		SerializationHandler<REF> methodWideSerializer = prototypeWideSerializer.withAdditionalSerializers(getSerializers(method));

		return handlerFor(method, StudentSidePrototypeMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
		{
			case CONSTRUCTOR -> constructorHandler(method, methodWideSerializer, nameOverridden);
			case STATIC_METHOD -> staticMethodHandler(method, methodWideSerializer, name);
			case STATIC_FIELD_GETTER -> staticFieldGetterHandler(method, methodWideSerializer, name);
			case STATIC_FIELD_SETTER -> staticFieldSetterHandler(method, methodWideSerializer, name);
		});
	}

	private MethodHandler constructorHandler(Method method, SerializationHandler<REF> methodWideSerializer, boolean nameOverridden)
	{
		switch(objectClass.getAnnotation(StudentSideObjectKind.class).value())
		{
			case CLASS:
				break;
			case INTERFACE:
				throw new InconsistentHierarchyException("Student-side interfaces can't have constructors");
			default:
				throw new IllegalStateException("Unknown student-side object kind: "
						+ objectClass.getAnnotation(StudentSideObjectKind.class).value());
		}

		if(nameOverridden)
			throw new InconsistentHierarchyException("Student-side constructor had name overridden: " + method);

		if(!method.getReturnType().equals(objectClass))
			throw new InconsistentHierarchyException("Student-side constructor return type wasn't the associated student-side object class: " +
					"expected " + objectClass + ", but was " + method.getReturnType() + ": " + method);

		List<Class<?>> constrParamTypes = Arrays.asList(method.getParameterTypes());
		List<String> constrParamCNs = c2n(constrParamTypes);
		return (proxy, args) ->
		{
			List<REF> argRefs = methodWideSerializer.send(constrParamTypes, argsToList(args));
			REF instanceRef = communicator.callConstructor(studentSideCN, constrParamCNs, argRefs);
			return objectBuilder.createInstance(instanceRef);
		};
	}

	private MethodHandler staticMethodHandler(Method method, SerializationHandler<REF> methodWideSerializer, String name)
	{
		Class<?> returnType = method.getReturnType();

		String returnCN = c2n(returnType);
		List<Class<?>> paramClasses = Arrays.asList(method.getParameterTypes());
		List<String> paramCNs = c2n(paramClasses);

		return (proxy, args) ->
		{
			List<REF> argRefs = methodWideSerializer.send(paramClasses, argsToList(args));
			REF resultRef = communicator.callStaticMethod(studentSideCN, name, returnCN, paramCNs, argRefs);
			return methodWideSerializer.receive(returnType, resultRef);
		};
	}

	private MethodHandler staticFieldGetterHandler(Method method, SerializationHandler<REF> methodWideSerializer, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters: " + method);

		String returnCN = c2n(returnType);

		return (proxy, args) ->
		{
			REF resultRef = communicator.getStaticField(studentSideCN, name, returnCN);
			return methodWideSerializer.receive(returnType, resultRef);
		};
	}

	private MethodHandler staticFieldSetterHandler(Method method, SerializationHandler<REF> methodWideSerializer, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + method);

		Class<?> paramType = paramTypes[0];

		return staticFieldSetterHandlerChecked(methodWideSerializer, name, paramType);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler staticFieldSetterHandlerChecked(SerializationHandler<REF> methodWideSerializer, String name, Class<F> fieldType)
	{
		String fieldCN = c2n(fieldType);

		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			REF valRef = methodWideSerializer.send(fieldType, argCasted);
			communicator.setStaticField(studentSideCN, name, fieldCN, valRef);
			return null;
		};
	}
}
