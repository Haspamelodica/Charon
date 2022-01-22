package net.haspamelodica.studentcodeseparator.impl;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.c2n;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.Serializer;
import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideObject;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind.ObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.UseSerializer;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;

// TODO find better names for StudentSideObject/Prototype and configuration annotations.
// Problem: Superclasses/interfaces.
// .Idea: Specify using regular Java superinterfaces: Student-side object class extends other student-side object class
// ..Problem: What if a student class is reqired to override a class / interface from the standard library?
// ...Idea: Use a prototype for that class.
// ...Sub-problem: What if the student object should be passed to a standard library function (for example Collections.sort)?
// ....Idea: Don't do that tester-side, but student-side.
// Problem: Regular Java objects passed to student-side objects would have to be serialized. This shouldn't happen automatically.
// .Idea: Specify serializers to use with annotations and provide default serializers for usual classes (String, List, Set, Map...)
// ..Problem: what about non-immutable datastructures?
// .Idea: specify default prototypes. Problem: need to duplicate standard library interface.
// ..Benefit: Handles non-immutable datastructures fine.
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
		SerializationHandler<REF> serializer = new SerializationHandler<>(communicator)
				.withAdditionalSerializers(getSerializers(prototypeClass))
				.withAdditionalSerializers(getSerializers(objectClass));
		String studentSideCN = getName(objectClass);

		return createProxyInstance(prototypeClass, method ->
		{
			checkNotAnnotatedWith(method, StudentSideObjectKind.class);
			checkNotAnnotatedWith(method, StudentSideObjectMethodKind.class);
			SerializationHandler<REF> serializerMethod = serializer.withAdditionalSerializers(getSerializers(method));

			return handlerFor(method, StudentSidePrototypeMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
			{
				case CONSTRUCTOR -> constructorHandler(objectClass, serializer, serializerMethod, studentSideCN, method, nameOverridden);
				case STATIC_METHOD -> staticMethodHandler(studentSideCN, serializerMethod, method, name);
				case STATIC_FIELD_GETTER -> staticFieldGetterHandler(studentSideCN, serializerMethod, method, name);
				case STATIC_FIELD_SETTER -> staticFieldSetterHandler(studentSideCN, serializerMethod, method, name);
			});
		});
	}

	private static <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
			Class<SO> checkAndGetObjectClassFromPrototypeClass(Class<SP> prototypeClass)
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

					//From the method signature, we know the type parameter to StudentSidePrototype is SO.
					//So, this cast has to succeed.
					@SuppressWarnings("unchecked")
					Class<SO> objectClass = (Class<SO>) objectTypeUnchecked;
					checkObjectClass(objectClass);
					return objectClass;
				}
		throw new InconsistentHierarchyException("A prototype class has to implement StudentClassPrototype directly: " + prototypeClass);
	}

	private static <SO extends StudentSideObject> void checkObjectClass(Class<SO> objectClass)
	{
		checkNotAnnotatedWith(objectClass, StudentSideObjectMethodKind.class);
		checkNotAnnotatedWith(objectClass, StudentSidePrototypeMethodKind.class);

		StudentSideObjectKind kind = objectClass.getAnnotation(StudentSideObjectKind.class);
		if(kind == null)
			throw new InconsistentHierarchyException("A student-side object class has to be annotated with StudentSideObjectKind: " + objectClass);
		if(kind.value() != ObjectKind.CLASS)
			throw new IllegalArgumentException("Student-side interfaces aren't implemented yet");

		//TODO verify object class
	}

	private <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>>
			TypedMethodHandler<SP> constructorHandler(Class<SO> objectClass, SerializationHandler<REF> serializer,
					SerializationHandler<REF> serializerMethod, String studentSideCN, Method method, boolean nameOverridden)
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
		return (proxy, args) -> createInstance(objectClass, serializer, serializerMethod, studentSideCN, constrParamTypes, constrParamCNs, args);
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticMethodHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();

		String returnCN = c2n(returnType);
		List<Class<?>> paramClasses = Arrays.asList(method.getParameterTypes());
		List<String> paramCNs = c2n(paramClasses);

		return (proxy, args) -> serializer.receive(returnType, communicator.callStaticMethod(
				studentSideCN, name, returnCN, paramCNs, serializer.send(paramClasses, argsToList(args))));
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticFieldGetterHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side static field getter had parameters: " + method);

		String returnCN = c2n(returnType);

		return (proxy, args) -> serializer.receive(returnType, communicator.getStaticField(studentSideCN, name, returnCN));
	}

	private <SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticFieldSetterHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side static field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side static field setter had not exactly one parameter: " + method);

		Class<?> paramType = paramTypes[0];

		return staticFieldSetterHandlerChecked(studentSideCN, serializer, name, paramType);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F, SP extends StudentSidePrototype<?>> TypedMethodHandler<SP>
			staticFieldSetterHandlerChecked(String studentSideCN, SerializationHandler<REF> serializer, String name, Class<F> fieldType)
	{
		String fieldCN = c2n(fieldType);

		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			communicator.setStaticField(studentSideCN, name, fieldCN, serializer.send(fieldType, argCasted));
			return null;
		};
	}

	private <SO extends StudentSideObject> SO
			createInstance(Class<SO> objectClass, SerializationHandler<REF> serializer,
					SerializationHandler<REF> serializerConstrMethod, String studentSideCN, List<Class<?>> constrParamTypes,
					List<String> constrParamCNs, Object... constrArgs)
	{
		REF ref = communicator.callConstructor(studentSideCN, constrParamCNs, serializerConstrMethod.send(constrParamTypes, argsToList(constrArgs)));
		return createProxyInstance(objectClass, method ->
		{
			checkNotAnnotatedWith(method, StudentSideObjectKind.class);
			checkNotAnnotatedWith(method, StudentSidePrototypeMethodKind.class);
			SerializationHandler<REF> serializerMethod = serializer.withAdditionalSerializers(getSerializers(method));

			return handlerFor(method, StudentSideObjectMethodKind.class, (kind, name, nameOverridden) -> switch(kind.value())
			{
				case INSTANCE_METHOD -> instanceMethodHandler(studentSideCN, serializerMethod, method, name, ref);
				case FIELD_GETTER -> fieldGetterHandler(studentSideCN, serializerMethod, method, name, ref);
				case FIELD_SETTER -> fieldSetterHandler(studentSideCN, serializerMethod, method, name, ref);
			});
		});
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO>
			instanceMethodHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name, REF ref)
	{
		Class<?> returnType = method.getReturnType();
		List<Class<?>> paramTypes = Arrays.asList(method.getParameterTypes());

		String returnCN = c2n(returnType);
		List<String> paramCNs = c2n(paramTypes);

		return (proxy, args) -> serializer.receive(returnType, communicator.callInstanceMethod(studentSideCN, name, returnCN, paramCNs, ref,
				serializer.send(paramTypes, argsToList(args))));
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO>
			fieldGetterHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name, REF ref)
	{
		Class<?> returnType = method.getReturnType();
		if(returnType.equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field getter return type was void: " + method);

		if(method.getParameterTypes().length != 0)
			throw new InconsistentHierarchyException("Student-side instance field getter had parameters: " + method);

		String returnCN = c2n(returnType);

		return (proxy, args) -> serializer.receive(returnType, communicator.getField(studentSideCN, name, returnCN, ref));
	}

	private <SO extends StudentSideObject> TypedMethodHandler<SO>
			fieldSetterHandler(String studentSideCN, SerializationHandler<REF> serializer, Method method, String name, REF ref)
	{
		if(!method.getReturnType().equals(void.class))
			throw new InconsistentHierarchyException("Student-side instance field setter return type wasn't void:" + method);

		Class<?>[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1)
			throw new InconsistentHierarchyException("Student-side instance field setter had not exactly one parameter: " + method);

		Class<?> paramType = paramTypes[0];

		return fieldSetterHandlerChecked(studentSideCN, serializer, name, paramType, ref);
	}

	//extracted to own method so casting to field type is expressible in Java
	private <F, SO extends StudentSideObject> TypedMethodHandler<SO>
			fieldSetterHandlerChecked(String studentSideCN, SerializationHandler<REF> serializer, String name, Class<F> fieldType, REF ref)
	{
		String fieldCN = c2n(fieldType);

		return (proxy, args) ->
		{
			@SuppressWarnings("unchecked")
			F argCasted = (F) args[0];
			communicator.setField(studentSideCN, name, fieldCN, ref, serializer.send(fieldType, argCasted));
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
				throw new InconsistentHierarchyException("Method is abstract, but has no special student-side meaning: " + method);

			handler = generateStudentSideHandler.generate(kind, getName(method), false);
		} else
		{
			if(kind != null)
				throw new InconsistentHierarchyException("Method is not abstract, but has a special student-side meaning: " + method);

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

	private static List<Object> argsToList(Object[] args)
	{
		return args == null ? List.of() : Arrays.asList(args);
	}

	private static List<Class<? extends Serializer<?>>> getSerializers(AnnotatedElement element)
	{
		//This also catches uses of UseSerializers
		return Arrays.stream(element.getAnnotationsByType(UseSerializer.class))
				.map((Function<UseSerializer, Class<? extends Serializer<?>>>) UseSerializer::value).toList();
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

	private static void checkNotAnnotatedWith(AnnotatedElement annotatedObject, Class<? extends Annotation> annotationClass)
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
