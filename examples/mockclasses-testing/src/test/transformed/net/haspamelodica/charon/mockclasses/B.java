package net.haspamelodica.charon.mockclasses;

import java.util.function.Supplier;

public class B
{
	public A testCyclicType(Supplier<A> newA)
	{
		System.out.println("B.testCyclicType");
		return new A();
	}
}
