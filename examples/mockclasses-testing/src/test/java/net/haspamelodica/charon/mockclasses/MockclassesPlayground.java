package net.haspamelodica.charon.mockclasses;

public class MockclassesPlayground
{
	public static void main(String[] args)
	{
		System.out.println("Playground classloader: " + MockclassesPlayground.class.getClassLoader());
		Transformed.run();
		Transformed transformed = new Transformed();
		transformed.runInstanceMethod();
		transformed.testCyclicType().testCyclicType().testCyclicType().testCyclicType();
	}
}
