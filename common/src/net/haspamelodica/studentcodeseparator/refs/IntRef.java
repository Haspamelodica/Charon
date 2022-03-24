package net.haspamelodica.studentcodeseparator.refs;

public class IntRef<ATTACHMENT> extends Ref<ATTACHMENT>
{
	private final int id;

	public IntRef(int id)
	{
		this.id = id;
	}

	public int id()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return "Ref#" + id;
	}
}
