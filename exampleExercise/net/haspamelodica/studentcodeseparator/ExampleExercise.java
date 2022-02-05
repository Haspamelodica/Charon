package net.haspamelodica.studentcodeseparator;

import net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.SameJVMCommunicator;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;

public class ExampleExercise
{
	public static void main(String[] args)
	{
		// An instance of StudentSide would be provided by Ares, not created by the tester.
		// Also, Ares would not use a SameJVMCommunicator in the tester JVM.
		StudentSide studentSide = new StudentSideImpl<>(new LoggingCommunicator<>(new SameJVMCommunicator()));

		// The StudentSide can (only) be used to obtain instances (implementations) of Prototypes.
		// Prototypes provide access to everything static of a class:
		// constructors, static fields, static methods.
		MyClass.Prototype MyClassP = studentSide.createPrototype(MyClass.Prototype.class);
		ReferencingClass.Prototype ReferencingClassP = studentSide.createPrototype(ReferencingClass.Prototype.class);

		System.out.println("EXERCISE: --- Testing student-side static things");
		// A prototype can be used to call static methods, ...
		System.out.println("EXERCISE: staticMethod() returned " + MyClassP.staticMethod());
		// ...to set static fields, ...
		System.out.println("EXERCISE: Setting myStaticField to \"hello\"");
		MyClassP.myStaticField("hello");
		System.out.println("EXERCISE: staticMethod() returned " + MyClassP.staticMethod());
		// ... and to read static fields.
		System.out.println("EXERCISE: myStaticField has value \"" + MyClassP.myStaticField() + "\"");

		System.out.println("\nEXERCISE: --- Testing student-side non-static things");
		// A prototype can also be used to create instances of student-side instances (SSIs).
		System.out.println("EXERCISE: Creating instance with \"Hello World\"");
		MyClass instance = MyClassP.new_("Hello World");

		// A SSI can be used to call instance methods, to set instance fields, and to read instance fields.
		System.out.println("EXERCISE: myField has value \"" + instance.myField() + "\"");
		System.out.println("EXERCISE: Setting myField to \"foobar\"");
		instance.myField("foobar");
		instance.method();
		System.out.println("EXERCISE: myField has value \"" + instance.myField() + "\"");

		// The names in the exercise-side prototypes / SSIs don't have to match those in the student classes:
		// They can be overridden (exercise-side) using an annotation.
		System.out.println("EXERCISE: thirdMethod(\"test\") returned " + instance.thirdMethod("test"));

		//TODO description
		MyClass instanceFromStudent = ReferencingClassP.createImpl();
		System.out.println("EXERCISE: createImpl().myField is \"" + instanceFromStudent.myField() + "\"");
		System.out.println("EXERCISE: myClassImplToString(instance) is \"" + ReferencingClassP.myClassImplToString(instance) + "\"");
		System.out.println("EXERCISE: myClassImplToString(instanceFromStudent) is \"" + ReferencingClassP.myClassImplToString(instanceFromStudent) + "\"");

		System.out.println("\nEXERCISE: --- Testing non-abstract methods");
		// Prototype classes (and SSI classes) can contain methods
		// implemented in the prototype / SSI class itself, although I'm not sure where this would be useful.
		System.out.println(MyClassP.test2());
	}
}
