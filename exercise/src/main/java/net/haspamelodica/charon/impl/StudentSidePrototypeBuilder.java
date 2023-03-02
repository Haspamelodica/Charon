package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.argsToList;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.checkNotAnnotatedWith;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.createProxyInstance;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.handlerFor;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.mapToStudentSide;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.impl.StudentSideImplUtils.StudentSideType;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;

public final class StudentSidePrototypeBuilder<SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
{
	public final Class<SP> prototypeClass;

	public final Class<SI>				instanceClass;
	public final StudentSideType<SI>	instanceStudentSideType;

	public final MarshalingCommunicator<?> prototypeWideMarshalingCommunicator;

	public final StudentSideInstanceBuilder<SI> instanceBuilder;

	private final Map<Method, MethodHandler> methodHandlers;

	private final SP prototype;

	public StudentSidePrototypeBuilder(MarshalingCommunicator<?> globalMarshalingCommunicator, Class<SP> prototypeClass)
	{
		this.prototypeClass = prototypeClass;

		// The order of the following operations is important: each step depends on the last

		this.instanceClass = checkPrototypeClassAndGetInstsanceClass();
		this.instanceStudentSideType = mapToStudentSide(instanceClass);
		this.prototypeWideMarshalingCommunicator = createPrototypeWideMarshalingCommunicator(globalMarshalingCommunicator);

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

					// From the class type signature, we know SP's type parameter to StudentSidePrototype is SI.
					// So, this cast has to succeed.
					@SuppressWarnings("unchecked")
					Class<SI> instanceClass = (Class<SI>) instanceTypeUnchecked;
					return instanceClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement StudentClassPrototype directly: " + prototypeClass);
	}

	private MarshalingCommunicator<?> createPrototypeWideMarshalingCommunicator(MarshalingCommunicator<?> globalMarshalingCommunicator)
	{
		return globalMarshalingCommunicator
				.withAdditionalSerDeses(getSerDeses(prototypeClass))
				.withAdditionalSerDeses(getSerDeses(instanceClass));
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
		MarshalingCommunicator<?> methodWideMarshalingCommunicator = prototypeWideMarshalingCommunicator.withAdditionalSerDeses(getSerDeses(method));

		return handlerFor(method, StudentSidePrototypeMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
		{
			case CONSTRUCTOR -> constructorHandler(method, methodWideMarshalingCommunicator, nameOverridden);
			case STATIC_METHOD -> staticMethodHandler(method, methodWideMarshalingCommunicator, name);
			case STATIC_FIELD_GETTER -> staticFieldGetterHandler(method, methodWideMarshalingCommunicator, name);
			case STATIC_FIELD_SETTER -> staticFieldSetterHandler(method, methodWideMarshalingCommunicator, name);
		});
	}

	private MethodHandler constructorHandler(Method method, MarshalingCommunicator<?> marshalingCommunicator, boolean nameOverridden)
	{
		switch(instanceClass.getAnnotation(StudentSideInstanceKind.class).value())
		{
			case CLASS ->
					{
					}
			case INTERFACE -> throw new InconsistentHierarchyException("Student-side interfaces can't have constructors");
		}

		if(nameOverridden)
			throw new InconsistentHierarchyException("Student-side constructor had name overridden: " + method);

		if(!method.getReturnType().equals(instanceClass))
			throw new InconsistentHierarchyException("Student-side constructor return type wasn't the associated student-side instance class: " +
					"expected " + instanceClass + ", but was " + method.getReturnType() + ": " + method);

		List<StudentSideType<?>> params = mapToStudentSide(Arrays.asList(method.getParameterTypes()));
		return (proxy, args) -> marshalingCommunicator.callConstructor(instanceStudentSideType, params, argsToList(args));
	}

	private MethodHandler staticMethodHandler(Method method, MarshalingCommunicator<?> marshalingCommunicator, String name)
	{
		StudentSideType<?> returnType = mapToStudentSide(method.getReturnType());
		List<StudentSideType<?>> params = mapToStudentSide(Arrays.asList(method.getParameterTypes()));

		return (proxy, args) -> marshalingCommunicator.callStaticMethod(instanceStudentSideType, name, returnType, params, argsToList(args));
	}

	private MethodHandler staticFieldGetterHandler(Method method, MarshalingCommunicator<?> marshalingCommunicator, String name)
	{
		Class<?> localReturnType = method.getReturnType();
		if(localReturnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters: " + method);

		StudentSideType<?> fieldType = mapToStudentSide(localReturnType);

		return (proxy, args) -> marshalingCommunicator.getStaticField(instanceStudentSideType, name, fieldType);
	}

	private MethodHandler staticFieldSetterHandler(Method method, MarshalingCommunicator<?> marshalingCommunicator, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + method);

		return staticFieldSetterHandlerChecked(marshalingCommunicator, name, mapToStudentSide(paramTypes[0]));
	}

	// extracted to own method so casting to field type is expressible in Java
	private <F> MethodHandler staticFieldSetterHandlerChecked(MarshalingCommunicator<?> marshalingCommunicator, String name, StudentSideType<F> fieldType)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			marshalingCommunicator.setStaticField(instanceStudentSideType, name, fieldType, argCasted);
			return null;
		};
	}
}
