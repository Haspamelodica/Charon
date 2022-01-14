package net.haspamelodica.studentcodeseparator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideObject;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;

public class StudentSideImpl<REF> implements StudentSide
{
	private final ClassLoader					classLoader;
	private final StudentSideCommunicator<REF>	communicator;

	public StudentSideImpl(ClassLoader classLoader, StudentSideCommunicator<REF> communicator)
	{
		this.classLoader = classLoader;
		this.communicator = communicator;
	}

	@Override
	public <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>> SP createPrototype(Class<SP> prototypeClass)
	{
		Class<SO> objectClass = checkAndGetObjectClassFromPrototypeClass(prototypeClass);
		String studentSideClassName = getName(objectClass);

		return createProxyInstance(prototypeClass, method ->
		{
			checkNotAnnotatedWith(method, StudentSideObjectKind.class);
			checkNotAnnotatedWith(method, StudentSideObjectMethodKind.class);

			return handlerFor(method, StudentSidePrototypeMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
			{
				case CONSTRUCTOR -> constructorHandler(objectClass, studentSideClassName, method, nameOverridden);
				case STATIC_METHOD -> staticMethodHandler(studentSideClassName, method, name);
				case STATIC_FIELD_GETTER -> staticFieldGetterHandler(studentSideClassName, method, name);
				case STATIC_FIELD_SETTER -> staticFieldSetterHandler(studentSideClassName, method, name);
			});
		});
	}

	private static <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
			Class<SO> checkAndGetObjectClassFromPrototypeClass(Class<? extends StudentSidePrototype<SO>> prototypeClass)
	{
		if(!prototypeClass.isInterface())
			throw new InconsistentHierarchyException("Prototype classes have to be interfaces");

		for(Type genericSuperinterface : prototypeClass.getGenericInterfaces())
			if(genericSuperinterface.equals(StudentSidePrototype.class))
				throw new InconsistentHierarchyException("A prototype class has to give a type argument to StudentClassPrototype");
			else if(genericSuperinterface instanceof ParameterizedType parameterizedSuperinterface)
				if(parameterizedSuperinterface.getRawType() == StudentSidePrototype.class)
				{
					Type objectClassUnchecked = parameterizedSuperinterface.getActualTypeArguments()[0];
					if(!(objectClassUnchecked instanceof Class))
						throw new InconsistentHierarchyException("The type argument to StudentClassPrototype has to be an unparameterized or raw class");

					//TODO check object class
					@SuppressWarnings("unchecked")
					Class<SO> objectClass = (Class<SO>) objectClassUnchecked;
					return objectClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement StudentClassPrototype directly");
	}

	private <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
			TypedMethodHandler<SP> constructorHandler(Class<SO> objectClass, String studentSideClassName, Method method, boolean nameOverridden)
	{
		//TODO disallow if StudentSideObjectKind isn't CLASS

		if(nameOverridden)
			throw new InconsistentHierarchyException("Student-side constructor had name overridden");

		if(!method.getReturnType().equals(objectClass))
			throw new InconsistentHierarchyException("Student-side constructor return type wasn't the associated student-side object class: " +
					"expected " + objectClass + ", but was " + method.getReturnType());

		return (proxy, args) -> createInstance(objectClass, method.getParameterTypes(), args);
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticMethodHandler(String studentSideClassName, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		Class<?>[] parameterTypes = method.getParameterTypes();

		return (proxy, args) -> communicator.callStaticMethod(name, returnType, parameterTypes, args);
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticFieldGetterHandler(String studentSideClassName, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void");

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters");

		return (proxy, args) -> communicator.getStaticField(name, returnType);
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticFieldSetterHandler(String studentSideClassName, Method method, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method.getReturnType());

		Class<?>[] parameterTypes = method.getParameterTypes();
		if(parameterTypes.length != 0)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + parameterTypes.length);

		Class<?> parameterType = parameterTypes[0];

		return staticFieldSetterHandlerChecked(name, parameterType);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F, SP extends StudentSidePrototype<?>> TypedMethodHandler<SP> staticFieldSetterHandlerChecked(String name, Class<F> fieldType)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			communicator.setStaticField(name, fieldType, argCasted);
			return null;
		};
	}

	private <SO extends StudentSideObject> SO createInstance(Class<SO> objectClass, Class<?>[] constrParamTypes, Object... constrArgs)
	{
		REF ref = communicator.callConstructor(constrParamTypes, constrArgs);
		return createProxyInstance(objectClass, method ->
		{
			checkNotAnnotatedWith(method, StudentSideObjectKind.class);
			checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);

			return handlerFor(method, StudentSideObjectMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
			{
				case INSTANCE_METHOD -> instanceMethodHandler(method, name, ref);
				case FIELD_GETTER -> fieldGetterHandler(method, name, ref);
				case FIELD_SETTER -> fieldSetterHandler(method, name, ref);
			});
		});
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO> instanceMethodHandler(Method method, String name, REF ref)
	{
		Class<?> returnType = method.getReturnType();
		Class<?>[] parameterTypes = method.getParameterTypes();

		return (proxy, args) -> communicator.callInstanceMethod(name, returnType, parameterTypes, ref, args);
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO> fieldGetterHandler(Method method, String name, REF ref)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void");

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters");

		return (proxy, args) -> communicator.getField(name, returnType, ref);
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO> fieldSetterHandler(Method method, String name, REF ref)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method.getReturnType());

		Class<?>[] parameterTypes = method.getParameterTypes();
		if(parameterTypes.length != 0)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + parameterTypes.length);

		Class<?> parameterType = parameterTypes[0];

		return fieldSetterHandlerChecked(name, parameterType, ref);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F, SO extends StudentSideObject> TypedMethodHandler<SO> fieldSetterHandlerChecked(String name, Class<F> fieldType, REF ref)
	{
		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			communicator.setField(name, fieldType, ref, argCasted);
			return null;
		};
	}

	private <P, K extends Annotation> TypedMethodHandler<P>
			handlerFor(Method method, Class<K> kindClass, StudentSideHandlerGenerator<P, K> generateStudentSideHandler)
	{
		K kind = method.getAnnotation(kindClass);
		TypedMethodHandler<P> handler;
		if(Modifier.isAbstract(method.getModifiers()))
		{
			if(kind == null)
				throw new InconsistentHierarchyException(method + " is abstract, but has no special student-side meaning");

			//TODO allow name overriding
			handler = generateStudentSideHandler.generate(kind, method.getName(), false);
		} else
		{
			if(kind != null)
				throw new InconsistentHierarchyException(method + " is not abstract, but has a special student-side meaning");

			handler = (proxy, args) -> InvocationHandler.invokeDefault(proxy, method, args);
		}
		return handler;
	}

	private <P> P createProxyInstance(Class<P> proxiedClass, Function<Method, TypedMethodHandler<P>> handlerGenerator)
	{
		Map<Method, TypedMethodHandler<P>> methodHandlersM = new HashMap<>();
		for(Method method : proxiedClass.getDeclaredMethods())
			methodHandlersM.put(method, handlerGenerator.apply(method));

		Map<Method, TypedMethodHandler<P>> methodHandlers = Map.copyOf(methodHandlersM);
		//We could optimize this: After we checked, the handler could be a switch(method.getAnnotation(...).value()) instead of a Map lookup.
		return createProxyInstance(proxiedClass, (proxy, method, args) -> methodHandlers.get(method).invoke(proxy, args));
	}

	private <P> P createProxyInstance(Class<P> proxiedClass, TypedInvocationHandler<P> handler)
	{
		@SuppressWarnings("unchecked")
		P proxyInstance = (P) Proxy.newProxyInstance(classLoader, new Class[] {proxiedClass},
				(proxy, method, args) -> handler.invoke((P) proxy, method, args));
		return proxyInstance;
	}

	private static String getName(Class<?> clazz)
	{
		return getName(clazz, Class::getName);
	}
	private static String getName(Method method)
	{
		return getName(method, Method::getName);
	}
	private static <E extends AnnotatedElement> String getName(E element, Function<E, String> getName)
	{
		OverrideStudentSideName overrideStudentSideName = element.getAnnotation(OverrideStudentSideName.class);
		if(overrideStudentSideName != null)
			return overrideStudentSideName.value();

		return getName.apply(element);
	}

	private static void checkNotAnnotatedWith(AccessibleObject annotatedObject, Class<? extends Annotation> annotationClass)
	{
		if(annotatedObject.isAnnotationPresent(annotationClass))
			throw new InconsistentHierarchyException(annotatedObject + " is unexpectedly annotated with " + annotationClass);
	}

	private static interface TypedInvocationHandler<P>
	{
		public Object invoke(P proxy, Method method, Object... args) throws Throwable;
	}
	private static interface TypedMethodHandler<P>
	{
		public Object invoke(P proxy, Object... args) throws Throwable;
	}
	private static interface StudentSideHandlerGenerator<P, K>
	{
		public TypedMethodHandler<P> generate(K kind, String name, boolean nameOverridden);
	}
}
