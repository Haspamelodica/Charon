package net.haspamelodica.charon.mockclasses;

import java.util.function.Supplier;

public class A
{
	public B testCyclicType(Supplier<B> newB)
	{
		System.out.println("A.testCyclicType");
		return new B();
	}
}
