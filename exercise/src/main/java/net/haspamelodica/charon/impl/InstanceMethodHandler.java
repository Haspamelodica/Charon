package net.haspamelodica.charon.impl;

public interface InstanceMethodHandler<REF>
{
	public Object invoke(REF ref, Object proxy, Object[] args) throws Throwable;
}
