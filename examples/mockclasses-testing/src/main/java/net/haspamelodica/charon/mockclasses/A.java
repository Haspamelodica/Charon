package net.haspamelodica.charon.mockclasses;

public class A
{
	public B testCyclicType()
	{
		System.out.println("A.testCyclicType");
		return new B();
	}
}
