package net.haspamelodica.studentcodeseparator;

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
		System.out.println(" STUDENT: otherThirdMethod(\"" + input + "\")");
		return input.length();
	}
}
