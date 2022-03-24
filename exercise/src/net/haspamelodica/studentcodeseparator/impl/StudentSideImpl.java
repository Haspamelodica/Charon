package net.haspamelodica.studentcodeseparator.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.serialization.PrimitiveSerializer;
import net.haspamelodica.studentcodeseparator.serialization.SerializationHandler;

// TODO find better names for StudentSideInstance/Prototype and configuration annotations.
// TODO maybe provide syncWithStudentSide method for mutable serialized objects
// TODO maybe create classes with same interface like "real" student-side classes (or based on template classes); replaces Prototypes
// .Problem: Doesn't work for student-side instances of standard JDK classes which shouldn't serialized
// TODO Superclasses/interfaces.
// .Idea: Specify using regular Java superinterfaces: Student-side instance class extends other student-side instance class
// ..Problem: What if a student class is reqired to override a class / interface from the standard library?
// ...Idea: Use a prototype for that class.
// ...Sub-problem: What if the student instance should be passed to a standard library function (for example Collections.sort)?
// ....Idea: Don't do that tester-side, but student-side.
// Problem: Regular Java instances passed to student-side instances would have to be serialized. This shouldn't happen automatically.
// .Idea: Specify serializers to use with annotations and provide default serializers for usual classes (String, List, Set, Map...)
// ..Problem: what about non-immutable datastructures?
// .Idea: specify default prototypes. Problem: need to duplicate standard library interface.
// ..Benefit: Handles non-immutable datastructures fine.
public class StudentSideImpl<REF extends Ref<StudentSideInstance>> implements StudentSide
{
	private final StudentSideCommunicatorClientSide<StudentSideInstance, REF>	communicator;
	private final SerializationHandler<StudentSideInstance, REF>	globalSerializer;

	private final Map<Class<? extends StudentSidePrototype<?>>, StudentSidePrototype<?>> prototypes;

	private final Map<String, StudentSidePrototypeBuilder<REF, ?, ?>> prototypeBuildersByStudentSideClassname;

	public StudentSideImpl(StudentSideCommunicatorClientSide<StudentSideInstance, REF> communicator)
	{
		this.communicator = communicator;
		this.globalSerializer = new SerializationHandler<>(communicator, this::refForStudentSideInstance, this::studentSideInstanceForRef, PrimitiveSerializer.PRIMITIVE_SERIALIZERS);
		this.prototypes = new ConcurrentHashMap<>();
		this.prototypeBuildersByStudentSideClassname = new ConcurrentHashMap<>();
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
	{
		// computeIfAbsent would be nicer algorithmically, but results in very ugly generic casts

		// fast path. Not neccessary to be synchronized (Map might be in an invalid state during put) since we use ConcurrentMap.
		SP prototype = tryGetPrototype(prototypeClass);
		if(prototype != null)
			return prototype;

		StudentSidePrototypeBuilder<REF, SI, SP> prototypeBuilder = new StudentSidePrototypeBuilder<>(communicator, globalSerializer, prototypeClass);

		synchronized(prototypes)
		{
			// re-get to see if some other thread was faster
			prototype = tryGetPrototype(prototypeClass);
			if(prototype != null)
				return prototype;

			String studentSideCN = prototypeBuilder.instanceBuilder.studentSideCN;

			if(prototypeBuildersByStudentSideClassname.containsKey(studentSideCN))
			{
				StudentSidePrototypeBuilder<REF, ?, ?> otherPrototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
				if(otherPrototypeBuilder.instanceClass.equals(prototypeBuilder.instanceClass))
					throw new InconsistentHierarchyException("Two prototype classes for " + prototypeBuilder.instanceClass + ": " +
							prototypeClass + " and " + otherPrototypeBuilder.prototypeClass);
				else
					throw new InconsistentHierarchyException("Two student-side instance classes for " + studentSideCN + ": " +
							prototypeBuilder.instanceClass + " and " + otherPrototypeBuilder.instanceClass);
			}

			prototype = prototypeBuilder.getPrototype();
			prototypeBuildersByStudentSideClassname.put(studentSideCN, prototypeBuilder);
			prototypes.put(prototypeClass, prototype);
			return prototype;
		}
	}

	private <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP tryGetPrototype(Class<SP> prototypeClass)
	{
		StudentSidePrototype<?> prototypeGeneric = prototypes.get(prototypeClass);
		@SuppressWarnings("unchecked") // we only put corresponding pairs of classes and prototypes into availablePrototypes
		SP prototype = (SP) prototypeGeneric;
		return prototype;
	}

	private REF refForStudentSideInstance(StudentSideInstance studentSideInstance)
	{
		// Guaranteed by StudentSidePrototypeBuilder#getPrototype
		InvocationHandler invocationHandler = Proxy.getInvocationHandler(studentSideInstance);
		@SuppressWarnings("unchecked") // Guaranteed by StudentSidePrototypeBuilder#getPrototype
		StudentSideInstanceInvocationHandler<REF> invocationHandlerCasted = (StudentSideInstanceInvocationHandler<REF>) invocationHandler;
		return invocationHandlerCasted.getRef();
	}

	private StudentSideInstance studentSideInstanceForRef(REF ref)
	{
		// fast path
		StudentSideInstance studentSideInstance = ref.getAttachment();
		if(studentSideInstance != null)
			return studentSideInstance;

		//TODO if we support inheritance, we need to check super-class-names too
		String studentSideCN = communicator.getStudentSideClassname(ref);
		StudentSidePrototypeBuilder<REF, ?, ?> prototypeBuilder = prototypeBuildersByStudentSideClassname.get(studentSideCN);
		if(prototypeBuilder == null)
			return null;

		return prototypeBuilder.instanceBuilder.createInstance(ref);
	}
}
