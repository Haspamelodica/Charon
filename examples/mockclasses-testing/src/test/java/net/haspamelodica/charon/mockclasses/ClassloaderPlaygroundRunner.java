package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

public class ClassloaderPlaygroundRunner
{
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException
	{
		System.out.println("--- With CharonClassloader ---");
		URL url = new URL("file:target/classes/");
		ClassLoader classloader = new DynamicClassLoader<>(new ClasspathBasedDynamicInterfaceProvider(url), new PlaygroundInvocationHandler(), false);
		Class<?> clazz = classloader.loadClass(ClassloaderPlayground.class.getName());
		clazz.getMethod("main", String[].class).invoke(null, new Object[] {args});

		System.out.println("--- With system classloader ---");
		ClassloaderPlayground.main(args);
	}
}
