package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.doChecked;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.SafeForCallByStudent;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedRef;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.MissingSerDesException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

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
public class StudentSideImpl<REF> implements StudentSide
{
	private final MarshalingCommunicator<?> globalMarshalingCommunicator;

	private final UnidirectionalMap<StudentSidePrototype<?>, StudentSidePrototypeBuilder<?, ?>>	prototypeBuildersByPrototype;
	private final UnidirectionalMap<String, StudentSidePrototypeBuilder<?, ?>>					prototypeBuildersByStudentSideClassname;
	private final UnidirectionalMap<Class<?>, StudentSidePrototypeBuilder<?, ?>>				prototypeBuildersByInstanceClass;
	private final UnidirectionalMap<Class<?>, StudentSidePrototypeBuilder<?, ?>>				prototypeBuildersByPrototypeClass;

	public StudentSideImpl(UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		this.globalMarshalingCommunicator = new MarshalingCommunicator<>(communicator, new MarshalingCommunicatorCallbacks<REF, Method>()
		{
			@Override
			public Object createRepresentationObject(UntranslatedRef<REF> untranslatedRef)
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
					throws ExceptionInTargetException
			{
				return StudentSideImpl.this.callCallbackInstanceMethodChecked(callbackMethod, receiver, args);
			}
		}, PrimitiveSerDes.PRIMITIVE_SERDESES);

		this.prototypeBuildersByPrototype = UnidirectionalMap.builder().identityMap().concurrent().build();
		this.prototypeBuildersByStudentSideClassname = UnidirectionalMap.builder().concurrent().build();
		this.prototypeBuildersByInstanceClass = UnidirectionalMap.builder().concurrent().build();
		this.prototypeBuildersByPrototypeClass = UnidirectionalMap.builder().concurrent().build();

		StudentSidePrototype.DEFAULT_PROTOTYPES.forEach(this::createPrototypeCaptureSI);
	}

	// Neccessary to capture the type argument to StudentSidePrototype.
	private <SI extends StudentSideInstance> StudentSidePrototype<SI> createPrototypeCaptureSI(Class<? extends StudentSidePrototype<SI>> clazz)
			throws InconsistentHierarchyException, MissingSerDesException
	{
		return createPrototype(clazz);
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
			throws InconsistentHierarchyException, MissingSerDesException
	{
		// computeIfAbsent would be nicer algorithmically, but results in very ugly generic casts

		// fast path. Not neccessary to be synchronized (Map might be in an invalid state during put) since we use ConcurrentMap.
		SP prototypeIfExists = tryGetPrototype(prototypeClass);
		if(prototypeIfExists != null)
			return prototypeIfExists;

		synchronized(prototypeBuildersByPrototype)
		{
			// re-get to see if some other thread was faster
			prototypeIfExists = tryGetPrototype(prototypeClass);
			if(prototypeIfExists != null)
				return prototypeIfExists;

			StudentSidePrototypeBuilder<SI, SP> prototypeBuilder = new StudentSidePrototypeBuilder<>(globalMarshalingCommunicator, prototypeClass);
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

			prototypeBuildersByPrototype.put(prototypeBuilder.prototype, prototypeBuilder);
			prototypeBuildersByStudentSideClassname.put(studentSideCN, prototypeBuilder);
			prototypeBuildersByInstanceClass.put(prototypeBuilder.instanceClass, prototypeBuilder);
			prototypeBuildersByPrototypeClass.put(prototypeClass, prototypeBuilder);

			return prototypeBuilder.prototype;
		}
	}

	@Override
	public String getStudentSideClassname(StudentSideInstance ssi)
	{
		return globalMarshalingCommunicator.getClassname(ssi);
	}
	@Override
	public String getStudentSideClassname(StudentSidePrototype<?> prototype)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceStudentSideType.studentSideCN();
	}
	@Override
	public boolean isInstance(StudentSidePrototype<?> prototype, StudentSideInstance ssi)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceClass.isInstance(ssi);
	}
	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SI cast(SP prototype, StudentSideInstance ssi)
	{
		String prototypeStudentSideCn = getStudentSideClassname(prototype);

		if(!isInstance(prototype, ssi))
			throw new ExerciseCausedException("" +
					"The given StudentSideInstance has student-side type " + getStudentSideClassname(ssi)
					+ ", which is not an instance of student-side type " + prototypeStudentSideCn);

		@SuppressWarnings("unchecked")
		StudentSidePrototypeBuilder<SI, SP> studentSidePrototypeBuilder =
				(StudentSidePrototypeBuilder<SI, SP>) prototypeBuildersByPrototype.get(prototype);

		return studentSidePrototypeBuilder.instanceClass.cast(ssi);
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP tryGetPrototype(Class<SP> prototypeClass)
	{
		@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into the map
		StudentSidePrototypeBuilder<SI, SP> prototypeBuilderGeneric =
				(StudentSidePrototypeBuilder<SI, SP>) prototypeBuildersByPrototypeClass.get(prototypeClass);

		return prototypeBuilderGeneric == null ? null : prototypeBuilderGeneric.prototype;
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
		//TODO there's not only no superclass for Object.class, but also if studentSideCN refers to an interface.
		if(studentSideCN.equals(Object.class.getName()))
			//TODO maybe introduce a global prototype for Object
			return Stream.of();

		return Stream.concat(
				streamPrototypeBuilders(globalMarshalingCommunicator.getSuperclass(studentSideCN)),
				globalMarshalingCommunicator
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

			if(!method.isAnnotationPresent(SafeForCallByStudent.class))
				throw new StudentSideCausedException("Student side attempted to call a callback method which is not allowed to be called as a callback: "
						+ callbackMethodToString(clazz, name, params));
			return new CallbackMethod<>(clazz, method.getReturnType(), List.of(method.getParameterTypes()), getSerDeses(method), method);
		}

		throw new IllegalArgumentException("Method not found: " + callbackMethodToString(clazz, name, params));
	}

	private String callbackMethodToString(Class<?> clazz, String name, List<String> params)
	{
		return clazz.getName() + "." + name + "(" + params.stream().collect(Collectors.joining(", ")) + ")";
	}

	private Object callCallbackInstanceMethodChecked(CallbackMethod<Method> callbackMethod, Object receiver, List<Object> args)
			throws ExceptionInTargetException
	{
		return doChecked(() -> callbackMethod.methodData().invoke(receiver, args.toArray()));
	}
}
