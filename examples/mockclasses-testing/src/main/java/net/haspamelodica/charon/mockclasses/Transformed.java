package net.haspamelodica.charon.mockclasses;

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

	public A testCyclicType()
	{
		System.out.println("Transformed.testCyclicType");
		return new A();
	}

	public void printA(A a)
	{
		System.out.println(a);
	}
}
