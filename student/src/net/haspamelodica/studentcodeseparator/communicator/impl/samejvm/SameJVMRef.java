package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class SameJVMRef<ATTACHMENT> extends Ref<ATTACHMENT>
{
	private final Object obj;

	public SameJVMRef(Object obj)
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
