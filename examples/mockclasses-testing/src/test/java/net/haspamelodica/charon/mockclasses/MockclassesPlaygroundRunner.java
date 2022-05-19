package net.haspamelodica.charon.mockclasses;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInterfaceProvider;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class MockclassesPlaygroundRunner
{
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException, InterruptedException, IncorrectUsageException
	{
		System.out.println("--- With custom classloader ---");

		DynamicInterfaceProvider interfaceProvider = new ClasspathBasedDynamicInterfaceProvider(
				new URL("file:target/classes/"));

		try(WrappedClassLoaderForCharon wrappedClassloader = new WrappedClassLoaderForCharon(interfaceProvider, args))
		{
			Class<?> clazz = wrappedClassloader.getClassloader().loadClass(MockclassesPlayground.class.getName());
			clazz.getMethod("main", String[].class).invoke(null, new Object[] {args});

			System.out.println();
			System.out.println("--- With system classloader ---");

			MockclassesPlayground.main(args);
		}
	}
}
