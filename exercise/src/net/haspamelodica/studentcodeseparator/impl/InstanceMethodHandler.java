package net.haspamelodica.studentcodeseparator.impl;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface InstanceMethodHandler<REFERENT, REFERRER, REF extends Ref<REFERENT, REFERRER>>
{
	public Object invoke(REF ref, Object proxy, Object[] args) throws Throwable;
}
