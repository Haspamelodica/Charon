package net.haspamelodica.studentcodeseparator;

public class ReferencingClassImpl
{
	public static MyClassImpl createImpl()
	{
		return new MyClassImpl("myField from ReferencingClassImpl");
	}

	public static String myClassImplToString(MyClassImpl impl)
	{
		return "myField is " + impl.getMyField();
	}
}
