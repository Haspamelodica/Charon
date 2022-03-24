package net.haspamelodica.studentcodeseparator.refs;

public class DirectRef<ATTACHMENT> extends Ref<ATTACHMENT>
{
	private final Object obj;

	public DirectRef(Object obj)
	{
		this.obj = obj;
	}

	public Object obj()
	{
		return obj;
	}

	@Override
	public String toString()
	{
		return "Ref[" + obj.toString() + "]";
	}
}
