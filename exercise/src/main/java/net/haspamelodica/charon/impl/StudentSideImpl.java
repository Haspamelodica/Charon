package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.doChecked;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.CallbackCallable;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedRef;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;

// TODO find better names for StudentSideInstance/Prototype and configuration annotations.
// TODO maybe provide syncWithStudentSide method for mutable serialized objects
// TODO Superclasses/interfaces.
// .Idea: Specify using regular Java superinterfaces: Student-side instance class extends other student-side instance class
// ..Problem: What if a student class is reqired to override a class / interface from the standard library?
// ...Idea: Use a prototype for that class.
// ...Sub-problem: What if the student instance should be passed to a standard library function (for example Collections.sort)?
// ....Idea: Don't do that tester-side, but student-side.
// Problem: Regular Java instances passed to student-side instances would have to be serialized. This shouldn't happen automatically.
// .Idea: Specify SerDeses to use with annotations and provide default SerDeses for usual classes (String, List, Set, Map...)
// ..Problem: what about non-immutable datastructures?
// .Idea: specify default prototypes. Problem: need to duplicate standard library interface.
// ..Benefit: Handles non-immutable datastructures fine.
public class StudentSideImpl implements StudentSide
{
	private final MarshalingCommunicator<?> marshalingCommunicator;

	private final Map<Class<? extends StudentSidePrototype<?>>, StudentSidePrototype<?>>	prototypesByPrototypeClass;
	private final Map<String, StudentSidePrototypeBuilder<?, ?>>							prototypeBuildersByStudentSideClassname;
	private final Map<Class<?>, StudentSidePrototypeBuilder<?, ?>>							prototypeBuildersByInstanceClass;

	public StudentSideImpl(UninitializedStudentSideCommunicatorClientSide<?> communicator)
	{
		this.marshalingCommunicator = new MarshalingCommunicator<>(communicator, new MarshalingCommunicatorCallbacks<Method>()
		{
			@Override
			public <REF> Object createRepresentationObject(UntranslatedRef<REF> untranslatedRef)
			{
				return StudentSideImpl.this.createRepresentationObject(untranslatedRef);
			}

			@Override
			public String getCallbackInterfaceCn(Object exerciseSideObject)
			{
				return StudentSideImpl.this.getCallbackInterfaceCn(exerciseSideObject);
			}

			@Override
			public CallbackMethod<Method> lookupCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params, Object receiver)
			{
				return StudentSideImpl.this.lookupCallbackInstanceMethod(cn, name, returnClassname, params, receiver);
			}

			@Override
			public Object callCallbackInstanceMethodChecked(CallbackMethod<Method> callbackMethod, Object receiver, List<Object> args)
			{
				return StudentSideImpl.this.callCallbackInstanceMethodChecked(callbackMethod, receiver, args);
			}
		}, PrimitiveSerDes.PRIMITIVE_SERDESES);
		this.prototypesByPrototypeClass = new ConcurrentHashMap<>();
		this.prototypeBuildersByStudentSideClassname = new ConcurrentHashMap<>();
		this.prototypeBuildersByInstanceClass = new ConcurrentHashMap<>();
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
	{
		// computeIfAbsent would be nicer algorithmically, but results in very ugly generic casts

		// fast path. Not neccessary to be synchronized (Map might be in an invalid state during put) since we use ConcurrentMap.
		SP prototype = tryGetPrototype(prototypeClass);
		if(prototype != null)
			return prototype;

		StudentSidePrototypeBuilder<SI, SP> prototypeBuilder = new StudentSidePrototypeBuilder<>(marshalingCommunicator, prototypeClass);

		synchronized(prototypesByPrototypeClass)
		{
			// re-get to see if some other thread was faster
			prototype = tryGetPrototype(prototypeClass);
			if(prototype != null)
				return prototype;

			String studentSideCN = prototypeBuilder.instanceBuilder.instanceStudentSideType.studentSideCN();

			if(prototypeBuildersByStudentSideClassname.containsKey(studentSideCN))
			{
				StudentSidePrototypeBuilder<?, ?> otherPrototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
				if(otherPrototypeBuilder.instanceClass.equals(prototypeBuilder.instanceClass))
					throw new InconsistentHierarchyException("Two prototype classes for " + prototypeBuilder.instanceClass + ": " +
							prototypeClass + " and " + otherPrototypeBuilder.prototypeClass);
				else
					throw new InconsistentHierarchyException("Two student-side instance classes for " + studentSideCN + ": " +
							prototypeBuilder.instanceClass + " and " + otherPrototypeBuilder.instanceClass);
			}

			prototype = prototypeBuilder.getPrototype();
			prototypeBuildersByStudentSideClassname.put(studentSideCN, prototypeBuilder);
			prototypeBuildersByInstanceClass.put(prototypeBuilder.instanceClass, prototypeBuilder);
			prototypesByPrototypeClass.put(prototypeClass, prototype);
			return prototype;
		}
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP tryGetPrototype(Class<SP> prototypeClass)
	{
		StudentSidePrototype<?> prototypeGeneric = prototypesByPrototypeClass.get(prototypeClass);
		@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into availablePrototypes
		SP prototype = (SP) prototypeGeneric;
		return prototype;
	}

	private Object createRepresentationObject(UntypedUntranslatedRef untranslatedRef)
	{
		List<StudentSidePrototypeBuilder<?, ?>> prototypeBuilders = streamPrototypeBuilders(untranslatedRef.getClassname()).toList();
		if(prototypeBuilders.size() != 1)
			if(prototypeBuilders.size() == 0)
				throw new ExerciseCausedException("No prototype for " + untranslatedRef.getClassname());
			else
				//TODO try to support multiple prototypes
				throw new FrameworkCausedException("Multiple prototypes for " + untranslatedRef.getClassname());
		return prototypeBuilders.get(0).instanceBuilder.createInstance();
	}

	private Stream<StudentSidePrototypeBuilder<?, ?>> streamPrototypeBuilders(String studentSideCN)
	{
		StudentSidePrototypeBuilder<?, ?> prototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
		if(prototypeBuilder != null)
			return Stream.of(prototypeBuilder);
		if(studentSideCN.equals(Object.class.getName()))
			//TODO maybe introduce a global prototype for Object
			return Stream.of();

		return Stream.concat(
				streamPrototypeBuilders(marshalingCommunicator.getSuperclass(studentSideCN)),
				marshalingCommunicator
						.getInterfaces(studentSideCN)
						.stream()
						.flatMap(this::streamPrototypeBuilders));
	}

	private String getCallbackInterfaceCn(Object exerciseSideObject)
	{
		Class<?> clazz = exerciseSideObject.getClass();
		List<StudentSidePrototypeBuilder<?, ?>> prototypeBuilders = Arrays
				.stream(clazz.getInterfaces())
				.flatMap(interfaceClazz -> Stream.<StudentSidePrototypeBuilder<?, ?>> of(prototypeBuildersByInstanceClass.get(interfaceClazz)))
				.filter(Objects::nonNull)
				.toList();
		if(prototypeBuilders.size() != 1)
			if(prototypeBuilders.size() == 0)
				throw new ExerciseCausedException("No student side class for " + clazz);
			else
				//TODO try to support multiple callback interfaces
				throw new FrameworkCausedException("Multiple student side classes for " + clazz);

		return prototypeBuilders.get(0).instanceStudentSideType.studentSideCN();
	}

	private CallbackMethod<Method> lookupCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params, Object receiver)
	{
		Class<?> clazz = receiver.getClass();

		for(Method method : clazz.getMethods())
		{
			if(!method.getName().equals(name))
				continue;
			if(method.getParameterCount() != params.size())
				continue;
			for(int i = 0; i < method.getParameterCount(); i ++)
				if(!classToName(method.getParameterTypes()[i]).equals(params.get(i)))
					continue;
			if(!classToName(method.getReturnType()).equals(returnClassname))
				continue;

			if(!method.isAnnotationPresent(CallbackCallable.class))
				throw new StudentSideCausedException("Student side attempted to call a callback method which is not allowed to be called as a callback: "
						+ callbackMethodToString(clazz, name, params));
			return new CallbackMethod<>(clazz, method.getReturnType(), List.of(method.getParameterTypes()), method);
		}

		throw new IllegalArgumentException("Method not found: " + callbackMethodToString(clazz, name, params));
	}

	private String callbackMethodToString(Class<?> clazz, String name, List<String> params)
	{
		return clazz.getName() + "." + name + "(" + params.stream().collect(Collectors.joining(", ")) + ")";
	}

	private Object callCallbackInstanceMethodChecked(CallbackMethod<Method> callbackMethod, Object receiver, List<Object> args)
	{
		return doChecked(() -> callbackMethod.methodData().invoke(receiver, args.toArray()));
	}
}
