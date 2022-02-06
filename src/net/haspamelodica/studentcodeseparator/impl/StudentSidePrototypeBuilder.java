package net.haspamelodica.studentcodeseparator.impl;

import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.argsToList;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.createProxyInstance;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.getSerializers;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.getStudentSideName;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.studentcodeseparator.impl.StudentSideImplUtils.mapToStudentSide;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;
import net.haspamelodica.studentcodeseparator.serialization.SerializationHandler;

public final class StudentSidePrototypeBuilder<REF extends Ref<StudentSideInstance>, SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
{
	public final StudentSideCommunicator<StudentSideInstance, REF>	communicator;
	public final SerializationHandler<StudentSideInstance, REF>		globalSerializer;
	public final Class<SP>											prototypeClass;

	public final Class<SI>										instanceClass;
	public final String											studentSideCN;
	public final SerializationHandler<StudentSideInstance, REF>	prototypeWideSerializer;

	public final StudentSideInstanceBuilder<REF, SI> instanceBuilder;

	private final Map<Method, MethodHandler> methodHandlers;

	private final SP prototype;

	public StudentSidePrototypeBuilder(StudentSideCommunicator<StudentSideInstance, REF> communicator, SerializationHandler<StudentSideInstance, REF> globalSerializer,
			Class<SP> prototypeClass)
	{
		this.communicator = communicator;
		this.globalSerializer = globalSerializer;
		this.prototypeClass = prototypeClass;

		//The order of the following operations is important: each step depends on the last

		this.instanceClass = checkPrototypeClassAndGetInstsanceClass();
		this.studentSideCN = getStudentSideName(instanceClass);
		this.prototypeWideSerializer = createPrototypeWideSerializer();

		this.instanceBuilder = new StudentSideInstanceBuilder<>(this);

		this.methodHandlers = createMethodHandlers();

		this.prototype = createPrototype();
	}

	/**
	 * The guarantees in {@link StudentSideInstanceBuilder#createInstance(Object)} apply to all objects returned by student-side construcors
	 * of the returned prototype.
	 */
	public SP getPrototype()
	{
		return prototype;
	}

	private Class<SI> checkPrototypeClassAndGetInstsanceClass()
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

					//From the class type signature, we know SP's type parameter to StudentSidePrototype is SI.
					//So, this cast has to succeed.
					@SuppressWarnings("unchecked")
					Class<SI> instanceClass = (Class<SI>) instanceTypeUnchecked;
					return instanceClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement StudentClassPrototype directly: " + prototypeClass);
	}

	private SerializationHandler<StudentSideInstance, REF> createPrototypeWideSerializer()
	{
		return globalSerializer
				.withAdditionalSerializers(getSerializers(prototypeClass))
				.withAdditionalSerializers(getSerializers(instanceClass));
	}

	private Map<Method, MethodHandler> createMethodHandlers()
	{
		// We are guaranteed to catch all (relevant) methods this way: abstract interface methods have to be public
		return Arrays.stream(prototypeClass.getMethods())
				.collect(Collectors.toUnmodifiableMap(m -> m, this::methodHandlerFor));
	}

	private SP createPrototype()
	{
		return createProxyInstance(prototypeClass, (proxy, method, args) -> methodHandlers.get(method).invoke(proxy, args));
	}

	private MethodHandler methodHandlerFor(Method method)
	{
		checkNotAnnotatedWith(method, StudentSideInstanceKind.class);
		checkNotAnnotatedWith(method, StudentSideInstanceMethodKind.class);
		SerializationHandler<StudentSideInstance, REF> methodWideSerializer = prototypeWideSerializer.withAdditionalSerializers(getSerializers(method));

		return handlerFor(method, StudentSidePrototypeMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
		{
			case CONSTRUCTOR -> constructorHandler(method, methodWideSerializer, nameOverridden);
			case STATIC_METHOD -> staticMethodHandler(method, methodWideSerializer, name);
			case STATIC_FIELD_GETTER -> staticFieldGetterHandler(method, methodWideSerializer, name);
			case STATIC_FIELD_SETTER -> staticFieldSetterHandler(method, methodWideSerializer, name);
		});
	}

	private MethodHandler constructorHandler(Method method, SerializationHandler<StudentSideInstance, REF> methodWideSerializer, boolean nameOverridden)
	{
		switch(instanceClass.getAnnotation(StudentSideInstanceKind.class).value())
		{
			case CLASS:
				break;
			case INTERFACE:
				throw new InconsistentHierarchyException("Student-side interfaces can't have constructors");
			default:
				throw new IllegalStateException("Unknown student-side instance kind: "
						+ instanceClass.getAnnotation(StudentSideInstanceKind.class).value());
		}

		if(nameOverridden)
			throw new InconsistentHierarchyException("Student-side constructor had name overridden: " + method);

		if(!method.getReturnType().equals(instanceClass))
			throw new InconsistentHierarchyException("Student-side constructor return type wasn't the associated student-side instance class: " +
					"expected " + instanceClass + ", but was " + method.getReturnType() + ": " + method);

		List<Class<?>> constrParamTypes = Arrays.asList(method.getParameterTypes());
		List<String> constrParamCNs = mapToStudentSide(constrParamTypes);
		return (proxy, args) ->
		{
			List<REF> argRefs = methodWideSerializer.send(constrParamTypes, argsToList(args));
			REF instanceRef = communicator.callConstructor(studentSideCN, constrParamCNs, argRefs);
			return instanceBuilder.createInstance(instanceRef);
		};
	}

	private MethodHandler staticMethodHandler(Method method, SerializationHandler<StudentSideInstance, REF> methodWideSerializer, String name)
	{
		Class<?> returnType = method.getReturnType();

		String returnCN = mapToStudentSide(returnType);
		List<Class<?>> paramClasses = Arrays.asList(method.getParameterTypes());
		List<String> paramCNs = mapToStudentSide(paramClasses);

		return (proxy, args) ->
		{
			List<REF> argRefs = methodWideSerializer.send(paramClasses, argsToList(args));
			REF resultRef = communicator.callStaticMethod(studentSideCN, name, returnCN, paramCNs, argRefs);
			return methodWideSerializer.receive(returnType, resultRef);
		};
	}

	private MethodHandler staticFieldGetterHandler(Method method, SerializationHandler<StudentSideInstance, REF> methodWideSerializer, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters: " + method);

		String returnCN = mapToStudentSide(returnType);

		return (proxy, args) ->
		{
			REF resultRef = communicator.getStaticField(studentSideCN, name, returnCN);
			return methodWideSerializer.receive(returnType, resultRef);
		};
	}

	private MethodHandler staticFieldSetterHandler(Method method, SerializationHandler<StudentSideInstance, REF> methodWideSerializer, String name)
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
	private <F> MethodHandler staticFieldSetterHandlerChecked(SerializationHandler<StudentSideInstance, REF> methodWideSerializer, String name, Class<F> fieldType)
	{
		String fieldCN = mapToStudentSide(fieldType);

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
