package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.LoggingUtils.maybeWrapLogging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.WeakSameJVMRefManager;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;

public class ExampleExercise
{
	/**
	 * If you use {@link Mode#DATA_OTHER_JVM}, start {@link ExampleExerciseServer} first.
	 */
	private static final Mode MODE = Mode.DATA_OTHER_JVM;

	// HOST and PORT only matter for mode DATA_OTHER_JVM
	private static final String	HOST	= "localhost";
	private static final int	PORT	= ExampleExerciseServer.PORT;
	private enum Mode
	{
		DIRECT, DATA_SAME_JVM, DATA_OTHER_JVM;
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		// An instance of StudentSide would be provided by Ares, not created by the tester.
		// Also, Ares would not use a DirectSameJVMCommunicator (or DataCommunicatorServer) in the tester JVM.
		switch(MODE)
		{
			case DIRECT -> runDirect();
			case DATA_SAME_JVM -> runDataSameJVM();
			case DATA_OTHER_JVM -> runDataOtherJVM();
		}
	}

	private static void runDirect()
	{
		run(new StudentSideImpl<>(maybeWrapLogging(new DirectSameJVMCommunicator<>(new WeakSameJVMRefManager<>()))));
	}

	private static void runDataSameJVM() throws InterruptedException, IOException
	{
		try(PipedInputStream clientIn = new PipedInputStream(); PipedOutputStream clientOut = new PipedOutputStream())
		{
			Semaphore serverConnected = new Semaphore(0);
			new Thread(() ->
			{
				try(PipedInputStream serverIn = new PipedInputStream(clientOut); PipedOutputStream serverOut = new PipedOutputStream(clientIn))
				{
					serverConnected.release();
					DataCommunicatorServer server = new DataCommunicatorServer(new DataInputStream(serverIn), new DataOutputStream(serverOut),
							refManager -> maybeWrapLogging(new DirectSameJVMCommunicatorWithoutSerialization<>(refManager), "SERVER: "));
					server.run();
				} catch(IOException e)
				{
					throw new UncheckedIOException(e);
				} finally
				{
					// Might release twice; doesn't matter
					serverConnected.release();
				}
			}).start();
			// wait for the server to create PipedOutputStreams
			serverConnected.acquire();
			DataCommunicatorClient<StudentSideInstance> client = new DataCommunicatorClient<>(new DataInputStream(clientIn), new DataOutputStream(clientOut));
			run(new StudentSideImpl<>(maybeWrapLogging(client, "CLIENT: ")));
			client.shutdown();
		}
	}

	private static void runDataOtherJVM() throws IOException, UnknownHostException
	{
		try(Socket sock = new Socket(HOST, PORT);
				DataInputStream in = new DataInputStream(sock.getInputStream());
				DataOutputStream out = new DataOutputStream(sock.getOutputStream()))
		{
			DataCommunicatorClient<StudentSideInstance> client = new DataCommunicatorClient<>(in, out);
			run(new StudentSideImpl<>(maybeWrapLogging(client)));
			client.shutdown();
		}
	}

	private static void run(StudentSide studentSide)
	{
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
		System.out.println("\nEXERCISE: --- Testing passing SSIs");
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
