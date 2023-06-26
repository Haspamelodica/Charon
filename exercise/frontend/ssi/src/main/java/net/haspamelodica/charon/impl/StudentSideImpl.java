package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.impl.StudentSideImplUtils.getStudentSideName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.StudentSideType;
import net.haspamelodica.charon.annotations.SafeForCallByStudent;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedTyperef;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.ForStudentException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.MissingSerDesException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;
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
public class StudentSideImpl<REF, TYPEREF extends REF> implements StudentSide
{
	private final MarshalingCommunicator<REF, TYPEREF, StudentSideException> globalMarshalingCommunicator;

	private final UnidirectionalMap<StudentSidePrototype<?>, StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>>	prototypeBuildersByPrototype;
	private final UnidirectionalMap<String, StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>>					prototypeBuildersByStudentSideClassname;
	private final UnidirectionalMap<Class<?>, StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>>					prototypeBuildersByInstanceClass;
	private final UnidirectionalMap<Class<?>, StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>>					prototypeBuildersByPrototypeClass;

	public StudentSideImpl(UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		this.globalMarshalingCommunicator = new MarshalingCommunicator<>(communicator,
				new MarshalingCommunicatorCallbacks<REF, TYPEREF, Method, ThrowableSSI, StudentSideException>()
				{
					@Override
					public TYPEREF lookupCorrespondingStudentSideTypeForRepresentationClass(Class<?> representationClass, boolean throwIfNotFound)
					{
						return StudentSideImpl.this.lookupCorrespondingStudentSideTypeForRepresentationClass(representationClass, throwIfNotFound);
					}

					@Override
					public Object createRepresentationObject(UntranslatedRef<REF, TYPEREF> untranslatedRef)
					{
						return StudentSideImpl.this.createRepresentationObject(untranslatedRef);
					}

					@Override
					public String getCallbackInterfaceCn(Object exerciseSideObject)
					{
						return StudentSideImpl.this.getCallbackInterfaceCn(exerciseSideObject);
					}

					@Override
					public CallbackMethod<Method> lookupCallbackInstanceMethod(TYPEREF receiverStaticType, String name, TYPEREF returnType, List<TYPEREF> params,
							Class<?> receiverDynamicType)
					{
						return StudentSideImpl.this.lookupCallbackInstanceMethod(receiverStaticType, name, returnType, params, receiverDynamicType);
					}

					@Override
					public CallbackOperationOutcome<Object, ThrowableSSI> callCallbackInstanceMethodChecked(
							Method callbackMethod, Object receiver, List<Object> args)
					{
						return StudentSideImpl.this.callCallbackInstanceMethodChecked(callbackMethod, receiver, args);
					}

					@Override
					public ThrowableSSI checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject)
					{
						return representationObject instanceof ThrowableSSI throwableSSI ? throwableSSI : null;
					}

					@Override
					public StudentSideException createStudentCausedException(ThrowableSSI studentSideThrowable)
					{
						return new StudentSideException(studentSideThrowable, getStudentSideType(studentSideThrowable));
					}
				}, PrimitiveSerDes.PRIMITIVE_SERDESES);

		this.prototypeBuildersByPrototype = UnidirectionalMap.builder().identityMap().concurrent().build();
		this.prototypeBuildersByStudentSideClassname = UnidirectionalMap.builder().concurrent().build();
		this.prototypeBuildersByInstanceClass = UnidirectionalMap.builder().concurrent().build();
		this.prototypeBuildersByPrototypeClass = UnidirectionalMap.builder().concurrent().build();

		StudentSidePrototype.DEFAULT_PROTOTYPES.forEach(this::createPrototypeCast);
	}

	// We're only circumventing javac's type inference algorithm not catching this case.
	@SuppressWarnings("unchecked")
	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
			void createPrototypeCast(Class<? extends StudentSidePrototype<?>> prototypeClass)
	{
		createPrototype((Class<SP>) prototypeClass);
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
			throws InconsistentHierarchyException, MissingSerDesException
	{
		return getOrCreatePrototypeBuilder(prototypeClass).prototype;
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>>
			StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> getOrCreatePrototypeBuilder(Class<SP> prototypeClass)
	{
		// computeIfAbsent would be nicer algorithmically, but results in very ugly generic casts

		// fast path. Not neccessary to be synchronized (Map might be in an invalid state during put) since we use ConcurrentMap.
		@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into the map
		StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilderGeneric =
				(StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP>) prototypeBuildersByPrototypeClass.get(prototypeClass);
		if(prototypeBuilderGeneric != null)
			return prototypeBuilderGeneric;

		synchronized(prototypeBuildersByPrototype)
		{
			// re-get to see if some other thread was faster
			@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into the map
			StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilderGeneric2 =
					(StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP>) prototypeBuildersByPrototypeClass.get(prototypeClass);
			if(prototypeBuilderGeneric2 != null)
				return prototypeBuilderGeneric2;

			StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilder = new StudentSidePrototypeBuilder<>(globalMarshalingCommunicator, prototypeClass);
			Class<SI> instanceClass = prototypeBuilder.instanceClass;
			String studentSideCN = prototypeBuilder.instanceBuilder.studentSideType.name();

			if(prototypeBuildersByStudentSideClassname.containsKey(studentSideCN))
			{
				StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> otherPrototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
				if(otherPrototypeBuilder.instanceClass.equals(instanceClass))
					throw new InconsistentHierarchyException("Two prototype classes for " + instanceClass + ": " +
							prototypeClass + " and " + otherPrototypeBuilder.prototypeClass);
				else
					throw new InconsistentHierarchyException("Two student-side instance classes for " + studentSideCN + ": " +
							instanceClass + " and " + otherPrototypeBuilder.instanceClass);
			}

			prototypeBuildersByPrototype.put(prototypeBuilder.prototype, prototypeBuilder);
			prototypeBuildersByStudentSideClassname.put(studentSideCN, prototypeBuilder);
			prototypeBuildersByInstanceClass.put(instanceClass, prototypeBuilder);
			prototypeBuildersByPrototypeClass.put(prototypeClass, prototypeBuilder);

			return prototypeBuilder;
		}
	}

	@Override
	public StudentSideType getStudentSideType(StudentSidePrototype<?> prototype)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceBuilder.studentSideType;
	}

	@Override
	public StudentSideType getStudentSideType(StudentSideInstance ssi)
	{
		return new StudentSideTypeImpl<>(globalMarshalingCommunicator, globalMarshalingCommunicator.getTypeOf(ssi));
	}

	@Override
	public boolean isInstance(StudentSidePrototype<?> prototype, StudentSideInstance ssi)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceClass.isInstance(ssi);
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SI cast(SP prototype, StudentSideInstance ssi)
	{
		if(!isInstance(prototype, ssi))
			throw new ExerciseCausedException("" +
					"The given StudentSideInstance has student-side type " + getStudentSideType(ssi).name()
					+ ", which is not an instance of student-side type " + getStudentSideType(prototype).name());

		@SuppressWarnings("unchecked") // we checked this by calling isInstance
		SI ssiCasted = (SI) ssi;
		return ssiCasted;
	}

	private TYPEREF lookupCorrespondingStudentSideTypeForRepresentationClass(Class<?> representationClass, boolean throwIfNotFound)
	{
		StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> prototypeBuilder = prototypeBuildersByInstanceClass.get(representationClass);
		if(prototypeBuilder != null)
			return prototypeBuilder.instanceBuilder.studentSideType.getTyperef();

		//TODO this is ugly
		if(representationClass == StudentSideInstance.class)
			return globalMarshalingCommunicator.getTypeByNameAndVerify(Object.class.getName());

		if(StudentSideInstance.class.isAssignableFrom(representationClass))
		{
			@SuppressWarnings("unchecked") // checked with isAssignableFrom
			Class<? extends StudentSideInstance> instanceClass = (Class<? extends StudentSideInstance>) representationClass;
			Class<? extends StudentSidePrototype<?>> prototypeClassToUseForInstanceClass = StudentSidePrototypeBuilder.prototypeClassToUseForInstanceClass(instanceClass);
			return getOrCreatePrototypeBuilderGeneric(prototypeClassToUseForInstanceClass).instanceBuilder.studentSideType.getTyperef();
		}

		if(throwIfNotFound)
			throw new ExerciseCausedException("Tried using class which is neither a " + StudentSideInstance.class + " nor serializable: " + representationClass);
		else
			return null;
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>
			getOrCreatePrototypeBuilderGeneric(Class<? extends StudentSidePrototype<?>> prototypeClass)
	{
		@SuppressWarnings("unchecked") // responsibility of user
		Class<SP> prototypeClassCasted = (Class<SP>) prototypeClass;
		return getOrCreatePrototypeBuilder(prototypeClassCasted);
	}

	private StudentSideInstance createRepresentationObject(UntypedUntranslatedRef untranslatedRef)
	{
		return lookupPrototypeBuilder(untranslatedRef.getType()).instanceBuilder.createInstance();
	}

	private StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> lookupPrototypeBuilder(UntypedUntranslatedTyperef untranslatedTyperef)
	{
		List<StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>> prototypeBuilders = streamPrototypeBuilders(untranslatedTyperef).toList();
		if(prototypeBuilders.stream().distinct().count() != 1)
			if(prototypeBuilders.size() == 0)
				throw new ExerciseCausedException("No prototype for " + untranslatedTyperef.describe().name());
			else
				//TODO try to support multiple prototypes
				throw new FrameworkCausedException("Multiple prototypes for " + untranslatedTyperef.describe().name());
		StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> prototypeBuilder = prototypeBuilders.get(0);
		return prototypeBuilder;
	}

	private Stream<StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>> streamPrototypeBuilders(UntypedUntranslatedTyperef untranslatedTyperef)
	{
		StudentSideTypeDescription<? extends UntypedUntranslatedTyperef> description = untranslatedTyperef.describe();

		StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> prototypeBuilder = prototypeBuildersByStudentSideClassname.get(description.name());
		if(prototypeBuilder != null)
			return Stream.of(prototypeBuilder);

		return Stream.concat(
				description.superclass().map(this::streamPrototypeBuilders).orElse(Stream.empty()),
				description.superinterfaces().stream().flatMap(this::streamPrototypeBuilders));
	}

	private String getCallbackInterfaceCn(Object exerciseSideObject)
	{
		Class<?> clazz = exerciseSideObject.getClass();
		List<StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>> prototypeBuilders = Arrays
				.stream(clazz.getInterfaces())
				.flatMap(interfaceClazz -> Stream.<StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>> of(prototypeBuildersByInstanceClass.get(interfaceClazz)))
				.filter(Objects::nonNull)
				.toList();
		if(prototypeBuilders.size() != 1)
			if(prototypeBuilders.size() == 0)
				throw new ExerciseCausedException("No student side class for " + clazz);
			else
				//TODO try to support multiple callback interfaces
				throw new FrameworkCausedException("Multiple student side classes for " + clazz);

		return prototypeBuilders.get(0).instanceBuilder.studentSideType.name();
	}

	private CallbackMethod<Method> lookupCallbackInstanceMethod(TYPEREF receiverStaticType, String name, TYPEREF returnType, List<TYPEREF> params,
			Class<?> receiverDynamicType)
	{
		List<CallbackMethod<Method>> candidates = new ArrayList<>();
		List<Method> unsafeCandidates = new ArrayList<>();
		List<Method> unserializableCandidates = new ArrayList<>();

		for(Method method : receiverDynamicType.getMethods())
		{
			List<Class<? extends SerDes<?>>> serdeses = getSerDeses(method);

			MarshalingCommunicator<REF, TYPEREF, StudentSideException> methodWideMarshalingCommunicator =
					globalMarshalingCommunicator.withAdditionalSerDeses(serdeses);
			if(!getStudentSideName(method).equals(name))
				continue;
			Class<?>[] actualParams = method.getParameterTypes();
			Class<?> actualReturnType = method.getReturnType();
			if(actualParams.length != params.size())
				continue;
			// We have to compare TYPEREFs with == since they are REFs.
			// There's no need for a separate null check on the result of lookup since the param types / return type given to us will always be non-null;
			// so if a type can't be found, the corresponding check will fail, so the method won't be considered eligible.
			for(int i = 0; i < actualParams.length; i ++)
			{
				TYPEREF actualStudentSideParam = methodWideMarshalingCommunicator.lookupCorrespondingStudentSideTypeOrNull(actualParams[i]);
				if(actualStudentSideParam == null)
				{
					unserializableCandidates.add(method);
					continue;
				}
				if(actualStudentSideParam != params.get(i))
					continue;
			}
			if(methodWideMarshalingCommunicator.lookupCorrespondingStudentSideTypeOrNull(actualReturnType) != returnType)
				//TODO do we want to skip this method, throw an error, or warn?
				continue;

			if(method.isAnnotationPresent(SafeForCallByStudent.class))
				candidates.add(new CallbackMethod<>(serdeses, List.of(actualParams), actualReturnType, method));
			else
				unsafeCandidates.add(method);
		}

		int candidateCount = candidates.size();
		if(candidateCount == 1)
			return candidates.get(0);

		if(candidateCount > 1)
			throw new ExerciseCausedException("Multiple candidates found"
					+ " for callback method " + callbackMethodToString(receiverDynamicType, name, params));

		// There is no candidate which is marked as safe. At this point, it's just about giving the best error message.

		int unsafeCandidatesCount = unsafeCandidates.size();
		if(unsafeCandidatesCount == 1)
			// Is this an exercise caused exception or a student side caused exception?
			throw new StudentSideCausedException("Student side attempted to call a callback method which is not marked as safe for call by student:"
					+ callbackMethodToString(receiverDynamicType, name, params));

		if(unsafeCandidatesCount > 1)
			throw new ExerciseCausedException("Multiple candidates found, but none marked as safe for call by student,"
					+ " for callback method " + callbackMethodToString(receiverDynamicType, name, params));

		if(unserializableCandidates.size() > 0)
			throw new ExerciseCausedException("No candidate found for callback method except some with unserializable types: "
					+ callbackMethodToString(receiverDynamicType, name, params));

		throw new ExerciseCausedException("No candidate found"
				+ " for callback method " + callbackMethodToString(receiverDynamicType, name, params));
	}

	private String callbackMethodToString(Class<?> receiverDynamicType, String name, List<TYPEREF> params)
	{
		return receiverDynamicType.getName() + "." + name + "("
				+ params.stream().map(globalMarshalingCommunicator::describeType).map(StudentSideTypeDescription::name).collect(Collectors.joining(", ")) + ")";
	}

	private CallbackOperationOutcome<Object, ThrowableSSI> callCallbackInstanceMethodChecked(Method callbackMethod, Object receiver, List<Object> args)
	{
		try
		{
			return new CallbackOperationOutcome.Result<>(callbackMethod.invoke(receiver, args.toArray()));
		} catch(IllegalAccessException e)
		{
			throw new ExerciseCausedException("Charon doesn't have access to callback method: " + callbackMethod, e);
		} catch(InvocationTargetException e)
		{
			Throwable exerciseThrownException = e.getTargetException();
			if(exerciseThrownException instanceof ForStudentException forStudentException)
				return new CallbackOperationOutcome.Thrown<Object, ThrowableSSI>(forStudentException.getStudentSideCause());

			return new CallbackOperationOutcome.HiddenError<>();
		}
	}
}
