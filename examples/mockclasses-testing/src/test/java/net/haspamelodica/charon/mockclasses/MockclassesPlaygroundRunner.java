package net.haspamelodica.charon.mockclasses;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.ClasspathBasedDynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.WrappedMockclassStudentSide;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class MockclassesPlaygroundRunner
{
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException, InterruptedException, IncorrectUsageException
	{
		DynamicInterfaceProvider interfaceProvider = new ClasspathBasedDynamicInterfaceProvider(new URL("file:target/classes/"));
		try(WrappedMockclassStudentSide wrappedStudentSide = new WrappedMockclassStudentSide(interfaceProvider, args, MockclassesPlayground.class))
		{
			MockclassStudentSide studentSide = wrappedStudentSide.getStudentSide();

			System.out.println("--- With mockclasses ---");

			MockclassesPlayground playground = studentSide.createInstanceWithMockclasses(MockclassesPlayground.class, MockclassesPlaygroundImpl.class);
			runPlayground(playground);


			System.out.println();
			System.out.println("--- With system classloader ---");

			runPlayground(new MockclassesPlaygroundImpl());
		}
	}

	private static void runPlayground(MockclassesPlayground playground)
	{
		playground.run();
	}
}
