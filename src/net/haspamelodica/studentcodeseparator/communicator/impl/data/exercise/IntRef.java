package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

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
