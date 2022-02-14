package net.haspamelodica.studentcodeseparator.impl;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public interface InstanceMethodHandler<ATTACHMENT, REF extends Ref<ATTACHMENT>>
{
	public Object invoke(REF ref, Object proxy, Object[] args) throws Throwable;
}