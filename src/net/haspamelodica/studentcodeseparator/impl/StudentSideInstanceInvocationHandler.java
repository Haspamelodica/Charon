package net.haspamelodica.studentcodeseparator.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class StudentSideInstanceInvocationHandler<REF extends Ref> implements InvocationHandler
{
	private final Map<Method, InstanceMethodHandler<REF>> methodHandlers;

	private final REF ref;

	public StudentSideInstanceInvocationHandler(Map<Method, InstanceMethodHandler<REF>> methodHandlers, REF ref)
	{
		this.methodHandlers = methodHandlers;
		this.ref = ref;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		return methodHandlers.get(method).invoke(ref, proxy, args);
	}

	public REF getRef()
	{
		return ref;
	}
}
