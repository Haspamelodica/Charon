package net.haspamelodica.studentcodeseparator;

import net.haspamelodica.studentcodeseparator.communicator.impl.ReflectiveStudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;

public class ExampleExercise
{
	public static void main(String[] args)
	{
		// An instance of StudentSide would be provided by Ares, not created by the tester.
		// Also, Ares would not use a ReflectiveStudentSideCommunicator in the tester JVM.
		StudentSide studentSide = new StudentSideImpl<>(ExampleExercise.class.getClassLoader(),
				new ReflectiveStudentSideCommunicator());

		// The StudentSide can (only) be used to obtain instances (implementations) of Prototypes.
		// Prototypes provide access to everything static of a class:
		// constructors, static fields, static methods.
		MyClass.Prototype MyClassP = studentSide.createPrototype(MyClass.Prototype.class);

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
		// A prototype can also be used to create instances of student-side objects (SSOs).
		System.out.println("EXERCISE: Creating instance with \"Hello World\"");
		MyClass instance = MyClassP.new_("Hello World");

		// A SSO can be used to call instance methods, to set instance fields, and to read instance fields.
		System.out.println("EXERCISE: myField has value \"" + instance.myField() + "\"");
		System.out.println("EXERCISE: Setting myField to \"foobar\"");
		instance.myField("foobar");
		instance.method();
		System.out.println("EXERCISE: myField has value \"" + instance.myField() + "\"");

		// The names in the exercise-side prototypes / SSOs don't have to match those in the student classes:
		// They can be overridden (exercise-side) using an annotation.
		System.out.println("EXERCISE: thirdMethod(\"test\") returned " + instance.thirdMethod("test"));

		System.out.println("\nEXERCISE: --- Testing non-abstract methods");
		// Prototype classes (and SSO classes) can contain methods
		// implemented in the prototype / SSO class itself, although I'm not sure where this would be useful.
		System.out.println(MyClassP.test2());

	}
}
