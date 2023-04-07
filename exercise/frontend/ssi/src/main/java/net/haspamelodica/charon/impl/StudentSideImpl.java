package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.impl.StudentSideImplUtils.getSerDeses;
import static net.haspamelodica.charon.reflection.ReflectionUtils.doChecked;
import static net.haspamelodica.charon.reflection.ReflectionUtils.primitiveNameToClassOrThrow;

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
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedTyperef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedTyperef;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.MissingSerDesException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.marshaling.StudentSideType;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;
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
					public Class<?> lookupLocalType(UntranslatedTyperef<REF, TYPEREF> untranslatedTyperef)
					{
						return switch(untranslatedTyperef.describe().kind())
						{
							case CLASS, INTERFACE -> lookupPrototypeBuilder(untranslatedTyperef).instanceStudentSideType.localType();
							case ARRAY -> throw new UnsupportedOperationException("Arrays aren't supported by the SSI frontend yet");
							case PRIMITIVE -> primitiveNameToClassOrThrow(untranslatedTyperef.describe().name());
						};
					}

					@Override
					public Object createRepresentationObject(StudentSideType<TYPEREF, ?> type, UntranslatedRef<REF, TYPEREF> untranslatedRef)
					{
						return StudentSideImpl.this.createRepresentationObject(type, untranslatedRef);
					}

					@Override
					public String getCallbackInterfaceCn(Object exerciseSideObject)
					{
						return StudentSideImpl.this.getCallbackInterfaceCn(exerciseSideObject);
					}

					@Override
					public CallbackMethod<Method> lookupCallbackInstanceMethod(StudentSideType<TYPEREF, ?> receiverStaticType,
							Class<?> receiverDynamicType, String name, StudentSideType<TYPEREF, ?> returnType, List<StudentSideType<TYPEREF, ?>> params)
					{
						return StudentSideImpl.this.lookupCallbackInstanceMethod(receiverStaticType, receiverDynamicType, name, returnType, params);
					}

					@Override
					public Object callCallbackInstanceMethodChecked(Method callbackMethod, Object receiver, List<Object> args)
							throws ExceptionInTargetException
					{
						return StudentSideImpl.this.callCallbackInstanceMethodChecked(callbackMethod, receiver, args);
					}

					@Override
					public ThrowableSSI checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject)
					{
						return representationObject instanceof ThrowableSSI throwableSSI ? throwableSSI : null;
					}

					@Override
					public StudentSideException newStudentCausedException(ThrowableSSI studentSideThrowable)
					{
						return new StudentSideException(studentSideThrowable, globalMarshalingCommunicator.getTypeOf(studentSideThrowable).studentSideCN());
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

			StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilder = new StudentSidePrototypeBuilder<>(globalMarshalingCommunicator, prototypeClass);
			Class<SI> instanceClass = prototypeBuilder.instanceStudentSideType.localType();
			String studentSideCN = prototypeBuilder.instanceStudentSideType.studentSideCN();

			if(prototypeBuildersByStudentSideClassname.containsKey(studentSideCN))
			{
				StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> otherPrototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
				Class<?> otherInstanceClass = otherPrototypeBuilder.instanceStudentSideType.localType();
				if(otherInstanceClass.equals(instanceClass))
					throw new InconsistentHierarchyException("Two prototype classes for " + instanceClass + ": " +
							prototypeClass + " and " + otherPrototypeBuilder.prototypeClass);
				else
					throw new InconsistentHierarchyException("Two student-side instance classes for " + studentSideCN + ": " +
							instanceClass + " and " + otherPrototypeBuilder.instanceStudentSideType.localType());
			}

			prototypeBuildersByPrototype.put(prototypeBuilder.prototype, prototypeBuilder);
			prototypeBuildersByStudentSideClassname.put(studentSideCN, prototypeBuilder);
			prototypeBuildersByInstanceClass.put(instanceClass, prototypeBuilder);
			prototypeBuildersByPrototypeClass.put(prototypeClass, prototypeBuilder);

			return prototypeBuilder.prototype;
		}
	}

	@Override
	public String getStudentSideClassname(StudentSideInstance ssi)
	{
		return globalMarshalingCommunicator.getTypeOf(ssi).studentSideCN();
	}
	@Override
	public String getStudentSideClassname(StudentSidePrototype<?> prototype)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceStudentSideType.studentSideCN();
	}
	@Override
	public boolean isInstance(StudentSidePrototype<?> prototype, StudentSideInstance ssi)
	{
		return prototypeBuildersByPrototype.get(prototype).instanceStudentSideType.localType().isInstance(ssi);
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
		StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> studentSidePrototypeBuilder =
				(StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP>) prototypeBuildersByPrototype.get(prototype);

		return studentSidePrototypeBuilder.instanceStudentSideType.localType().cast(ssi);
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP tryGetPrototype(Class<SP> prototypeClass)
	{
		@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into the map
		StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP> prototypeBuilderGeneric =
				(StudentSidePrototypeBuilder<REF, TYPEREF, SI, SP>) prototypeBuildersByPrototypeClass.get(prototypeClass);

		return prototypeBuilderGeneric == null ? null : prototypeBuilderGeneric.prototype;
	}

	private Object createRepresentationObject(StudentSideType<TYPEREF, ?> type, UntypedUntranslatedRef untranslatedRef)
	{
		return prototypeBuildersByInstanceClass.get(type.localType()).instanceBuilder.createInstance();
	}

	private StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?> lookupPrototypeBuilder(UntypedUntranslatedTyperef untranslatedTyperef)
	{
		List<StudentSidePrototypeBuilder<REF, TYPEREF, ?, ?>> prototypeBuilders = streamPrototypeBuilders(untranslatedTyperef).toList();
		if(prototypeBuilders.size() != 1)
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

		return prototypeBuilders.get(0).instanceStudentSideType.studentSideCN();
	}

	private CallbackMethod<Method> lookupCallbackInstanceMethod(StudentSideType<TYPEREF, ?> receiverStaticType,
			Class<?> receiverDynamicType, String name, StudentSideType<TYPEREF, ?> returnType, List<StudentSideType<TYPEREF, ?>> params)
	{
		for(Method method : receiverDynamicType.getMethods())
		{
			if(!method.getName().equals(name))
				continue;
			if(method.getParameterCount() != params.size())
				continue;
			for(int i = 0; i < method.getParameterCount(); i ++)
				if(!method.getParameterTypes()[i].equals(params.get(i).localType()))
					continue;
			if(!method.getReturnType().equals(returnType.localType()))
				continue;

			if(!method.isAnnotationPresent(SafeForCallByStudent.class))
				throw new StudentSideCausedException("Student side attempted to call a callback method which is not allowed to be called as a callback: "
						+ callbackMethodToString(receiverDynamicType, name, params));
			return new CallbackMethod<>(getSerDeses(method), method);
		}

		throw new IllegalArgumentException("Method not found: " + callbackMethodToString(receiverDynamicType, name, params));
	}

	private String callbackMethodToString(Class<?> receiverDynamicType, String name, List<StudentSideType<TYPEREF, ?>> params)
	{
		return receiverDynamicType.getName() + "." + name + "("
				+ params.stream().map(StudentSideType::localType).map(Class::getName).collect(Collectors.joining(", ")) + ")";
	}

	private Object callCallbackInstanceMethodChecked(Method callbackMethod, Object receiver, List<Object> args)
			throws ExceptionInTargetException
	{
		return doChecked(() -> callbackMethod.invoke(receiver, args.toArray()));
	}
}
