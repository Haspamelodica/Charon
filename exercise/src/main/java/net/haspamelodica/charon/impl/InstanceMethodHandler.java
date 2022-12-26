package net.haspamelodica.charon.impl;

import net.haspamelodica.charon.refs.Ref;

public interface InstanceMethodHandler
{
	public Object invoke(Ref ref, Object proxy, Object[] args) throws Throwable;
}
