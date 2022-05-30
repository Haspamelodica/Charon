package net.haspamelodica.charon.mockclasses;

public class MockclassesPlaygroundImpl implements MockclassesPlayground
{
	public static void main(String[] args)
	{
		new MockclassesPlaygroundImpl().run();
	}

	@Override
	public void run()
	{
		System.out.println("Playground classloader: " + MockclassesPlaygroundImpl.class.getClassLoader());
		Transformed.run();
		Transformed transformed = new Transformed();
		transformed.runInstanceMethod();
		transformed.printA(transformed.testCyclicType().testCyclicType().testCyclicType().testCyclicType().testCyclicType());
	}
}
