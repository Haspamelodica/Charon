package net.haspamelodica.charon.impl;

public interface MethodHandler
{
	public Object invoke(Object proxy, Object[] args) throws Throwable;
}
