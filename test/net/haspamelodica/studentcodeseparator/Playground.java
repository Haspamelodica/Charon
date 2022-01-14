package net.haspamelodica.studentcodeseparator;

import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;

public class Playground
{
	public static void main(String[] args)
	{
		StudentSide studentSide = new StudentSideImpl<>(Playground.class.getClassLoader(), null);

		MyClass.Prototype prototype = studentSide.createPrototype(MyClass.Prototype.class);

		System.out.println("Testing non-abstract methods");
		System.out.println(prototype.test2());

		System.out.println("Testing student-side methods");
		MyClass test = prototype.new_();
		test.method();
		System.out.println(test.thirdMethod("test"));
	}
}
