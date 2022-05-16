package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.InvocationTargetException;

public class ClassloaderPlaygroundRunner
{
	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		System.out.println("--- With CharonClassloader ---");
		ClassLoader classloader = new DynamicClassLoader<>(new PlaygroundDynamicInterfaceProvider(), new PlaygroundInvocationHandler(), false);
		Class<?> clazz = classloader.loadClass(ClassloaderPlayground.class.getName());
		clazz.getMethod("main", String[].class).invoke(null, new Object[] {args});

		System.out.println("--- With system classloader ---");
		ClassloaderPlayground.main(args);
	}
}
