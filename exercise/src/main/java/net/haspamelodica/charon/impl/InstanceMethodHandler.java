package net.haspamelodica.charon.impl;

import net.haspamelodica.charon.refs.Ref;

public interface InstanceMethodHandler<REF extends Ref<?, ?>>
{
	public Object invoke(REF ref, Object proxy, Object[] args) throws Throwable;
}
