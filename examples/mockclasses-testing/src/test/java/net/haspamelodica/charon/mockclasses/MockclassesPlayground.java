package net.haspamelodica.charon.mockclasses;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide.MockclassesFunction;

public class MockclassesPlayground implements MockclassesFunction<Void, Void, RuntimeException>
{
	public static void main(String[] args)
	{
		new MockclassesPlayground().apply(null);
	}

	public Void apply(Void params)
	{
		System.out.println("Playground classloader: " + MockclassesPlayground.class.getClassLoader());
		Transformed.run();
		Transformed transformed = new Transformed();
		transformed.runInstanceMethod();
		transformed.testCyclicType().testCyclicType().testCyclicType().testCyclicType();

		return null;
	}
}
