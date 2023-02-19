package net.haspamelodica.charon.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class StudentSideInstanceInvocationHandler<REF> implements InvocationHandler
{
	private final Map<Method, InstanceMethodHandler<REF>> methodHandlers;

	private REF ref;

	public StudentSideInstanceInvocationHandler(Map<Method, InstanceMethodHandler<REF>> methodHandlers)
	{
		this.methodHandlers = methodHandlers;
	}

	public void setRef(REF ref)
	{
		Objects.requireNonNull(ref);
		if(this.ref != null)
			throw new IllegalStateException("Ref already set");
		this.ref = ref;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		return methodHandlers.get(method).invoke(ref, proxy, args);
	}
}
