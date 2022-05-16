package net.haspamelodica.charon.mockclasses;

public class ClassloaderPlayground
{
	public static void main(String[] args)
	{
		System.out.println("Playground classloader: " + ClassloaderPlayground.class.getClassLoader());
		Transformed.run();
		Transformed transformed = new Transformed();
		transformed.runInstanceMethod();
		transformed.testCyclicType(A::new)
		.testCyclicType(B::new).testCyclicType(A::new)
		.testCyclicType(B::new).testCyclicType(A::new)
		.testCyclicType(B::new).testCyclicType(A::new)
		.testCyclicType(B::new).testCyclicType(A::new).testCyclicType(B::new);
	}
}
