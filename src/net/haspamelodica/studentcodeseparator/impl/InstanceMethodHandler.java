package net.haspamelodica.studentcodeseparator.impl;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public interface InstanceMethodHandler<REF extends Ref>
{
	public Object invoke(REF ref, Object proxy, Object[] args) throws Throwable;
}
