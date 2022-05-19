package net.haspamelodica.charon.mockclasses;

public class B
{
	public A testCyclicType()
	{
		System.out.println("B.testCyclicType");
		return new A();
	}
}
