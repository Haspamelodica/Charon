package net.haspamelodica.charon.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import net.haspamelodica.charon.refs.Ref;

public class StudentSideInstanceInvocationHandler implements InvocationHandler
{
	private final Map<Method, InstanceMethodHandler> methodHandlers;

	private final Ref ref;

	public StudentSideInstanceInvocationHandler(Map<Method, InstanceMethodHandler> methodHandlers, Ref ref)
	{
		this.methodHandlers = methodHandlers;
		this.ref = ref;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		return methodHandlers.get(method).invoke(ref, proxy, args);
	}

	public Ref getRef()
	{
		return ref;
	}
}
