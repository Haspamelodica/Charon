package net.haspamelodica.charon;

public class ReferencingClassImpl
{
	private final MyClassImpl impl;

	public ReferencingClassImpl(MyClassImpl impl)
	{
		this.impl = impl;
	}

	public MyClassImpl getImpl()
	{
		return impl;
	}

	public static MyClassImpl createImpl()
	{
		return new MyClassImpl("myField from ReferencingClassImpl");
	}

	public static String myClassImplToString(MyClassImpl impl)
	{
		return "myField is " + impl.getMyField();
	}
}
