package net.haspamelodica.studentcodeseparator.impl;

import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.serialization.PrimitiveSerializer;
import net.haspamelodica.studentcodeseparator.serialization.SerializationHandler;

// TODO find better names for StudentSideInstance/Prototype and configuration annotations.
// Problem: Superclasses/interfaces.
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
public class StudentSideImpl<REF extends Ref> implements StudentSide
{
	private final StudentSideCommunicator<REF>	communicator;
	private final SerializationHandler<REF>		globalSerializer;

	public StudentSideImpl(StudentSideCommunicator<REF> communicator)
	{
		this.communicator = communicator;
		this.globalSerializer = new SerializationHandler<>(communicator, PrimitiveSerializer.PRIMITIVE_SERIALIZERS);
	}

	@Override
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
	{
		return new StudentSidePrototypeBuilder<>(communicator, globalSerializer, prototypeClass).getPrototype();
	}
}
