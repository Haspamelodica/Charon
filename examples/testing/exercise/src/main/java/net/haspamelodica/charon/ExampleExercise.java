package net.haspamelodica.charon;

import java.util.ArrayList;
import java.util.List;

import net.haspamelodica.charon.StudentSide;

public class ExampleExercise
{
	// To run, use ExampleExerciseClient in example.runner.
	public static void run(StudentSide studentSide)
	{
		// The framework provides an instance of StudentSide.
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

		// Apart from primitive types and types for which a serializer has been specified explicitly,
		// only SSIs can be passed directly to and returned from methods, since they represent student-side objects.
		// If you try to define a method with a parameter or return type which doesn't fall into these categories,
		// you will get a runtime exception.
		System.out.println("\nEXERCISE: --- Testing passing SSIs");
		MyClass instanceFromStudent = ReferencingClassP.createImpl();
		System.out.println("EXERCISE: createImpl().myField is \"" + instanceFromStudent.myField() + "\"");
		System.out.println("EXERCISE: myClassImplToString(instance) is \"" + ReferencingClassP.myClassImplToString(instance) + "\"");
		System.out.println("EXERCISE: myClassImplToString(instanceFromStudent) is \"" + ReferencingClassP.myClassImplToString(instanceFromStudent) + "\"");

		System.out.println("\nEXERCISE: --- Testing cleaning refs");
		System.out.println("EXERCISE: Forcing the race condition to occur");
		ReferencingClass wrappedInstance = ReferencingClassP.new_(ReferencingClassP.createImpl());
		for(int i = 0; i < 10; i ++)
		{
			System.gc();
			wrappedInstance.getImpl();
		}

		System.out.println("\nEXERCISE: --- Testing multithreading");
		System.out.println("\nEXERCISE: --- Testing blocking");
		instance.sendMessage("a");
		new Thread(() ->
		{
			instance.sendMessage("b");
			System.out.println("EXERCISE: Sent message b");
		}).start();
		try
		{
			Thread.sleep(1000);
		} catch(InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		System.out.println("EXERCISE: Got message " + instance.waitForMessage());
		System.out.println("EXERCISE: Got message " + instance.waitForMessage());
		System.out.println("\nEXERCISE: --- Testing interleaving threads");
		List<Thread> threads = new ArrayList<>();
		for(int i = 0; i < 10; i ++)
		{
			Thread thread = new Thread(() ->
			{
				for(int j = 0; j < 100; j ++)
					if(instance.thirdMethod("String") != 6)
						throw new IllegalStateException();
			});
			threads.add(thread);
			thread.start();
		}
		for(Thread thread : threads)
			try
			{
				thread.join();
			} catch(InterruptedException e)
			{
				throw new RuntimeException(e);
			}

		System.out.println("\nEXERCISE: --- Testing non-abstract methods");
		// Prototype classes (and SSI classes) can contain methods
		// implemented in the prototype / SSI class itself, although I'm not sure where this would be useful.
		System.out.println(MyClassP.test2());
	}
}
