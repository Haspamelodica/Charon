package net.haspamelodica.studentcodeseparator.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class StudentSideInstanceInvocationHandler<REF extends Ref<StudentSideInstance>> implements InvocationHandler
{
	private final Map<Method, InstanceMethodHandler<StudentSideInstance, REF>> methodHandlers;

	private final REF ref;

	public StudentSideInstanceInvocationHandler(Map<Method, InstanceMethodHandler<StudentSideInstance, REF>> methodHandlers, REF ref)
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
