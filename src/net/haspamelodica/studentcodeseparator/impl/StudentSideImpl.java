package net.haspamelodica.studentcodeseparator.impl;

import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideObject;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;

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
	private final StudentSideCommunicator<REF> communicator;

	public StudentSideImpl(StudentSideCommunicator<REF> communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>> SP createPrototype(Class<SP> prototypeClass)
	{
		return new StudentSidePrototypeBuilder<>(communicator, prototypeClass).createPrototype();
	}
}
