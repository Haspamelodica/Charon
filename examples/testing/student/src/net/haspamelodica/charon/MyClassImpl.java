package net.haspamelodica.charon;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyClassImpl
{
	private static String myStaticField = "Hello World";

	public static int staticMethod()
	{
		int result = myStaticField.length();
		System.out.println(" STUDENT: staticMethod() called; "
				+ "myStaticField is \"" + myStaticField + "\" (result is " + result + ")");

		myStaticField = "Changed by staticMethod()";
		return result;
	}

	private String myField;

	public MyClassImpl(String myField)
	{
		this.myField = myField + " with changes by student-side constructor";
	}

	public String getMyField()
	{
		return myField;
	}

	public void method()
	{
		System.out.println(" STUDENT: method() called. myField has value \"" + myField + "\"");
		myField = "myField changed by student";
	}

	public int otherThirdMethod(String input)
	{
		return input.length();
	}

	private final BlockingQueue<String> messages = new ArrayBlockingQueue<>(1);
	public String waitForMessage() throws InterruptedException
	{
		return messages.take();
	}
	public void sendMessage(String msg) throws InterruptedException
	{
		messages.put(msg);
	}
}
