package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class IntRef<ATTACHMENT> extends Ref<ATTACHMENT>
{
	private final int ref;

	public IntRef(int ref)
	{
		this.ref = ref;
	}

	public int ref()
	{
		return ref;
	}

	@Override
	public String toString()
	{
		return "Ref#" + ref;
	}
}
