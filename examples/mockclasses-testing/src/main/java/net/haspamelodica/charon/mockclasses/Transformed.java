package net.haspamelodica.charon.mockclasses;

import java.util.function.Supplier;

public class Transformed
{
	public Transformed()
	{
		System.out.println("Original constructor");
	}

	public static void run()
	{
		System.out.println("Transformed classloader: " + Transformed.class.getClassLoader());
		System.out.println(runInner());
	}

	private static String runInner()
	{
		return "Original inner run method";
	}

	public void runInstanceMethod()
	{
		System.out.println("Original instance method");
	}

	public A testCyclicType(Supplier<A> newA)
	{
		System.out.println("Transformed.testCyclicType");
		return new A();
	}
}
