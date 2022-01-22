package net.haspamelodica.studentcodeseparator.communicator.impl;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.n2c;

import java.util.List;

import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;

public class SameJVMCommunicator extends AbstractSameJVMCommunicator
{
	@Override
	public Object callConstructor(String cn, List<String> params, List<Object> argRefs)
	{
		return ReflectionUtils.callConstructor(n2c(cn), n2c(params), argRefs);
	}

	@Override
	public Object callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<Object> argRefs)
	{
		return ReflectionUtils.callStaticMethod(n2c(cn), name, n2c(returnClassname), n2c(params), argRefs);
	}

	@Override
	public Object getStaticField(String cn, String name, String fieldClassname)
	{
		return ReflectionUtils.getStaticField(n2c(cn), name, n2c(fieldClassname));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, Object valueRef)
	{
		setStaticField_(n2c(cn), name, n2c(fieldClassname), valueRef);
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public Object callInstanceMethod(String cn, String name, String returnClassname, List<String> params, Object receiverRef, List<Object> argRefs)
	{
		return callInstanceMethod_(n2c(cn), name, n2c(returnClassname), n2c(params), receiverRef, argRefs);
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object callInstanceMethod_(Class<T> clazz, String name, Class<?> returnClass, List<Class<?>> params, Object receiver, List<Object> args)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.callInstanceMethod(clazz, name, returnClass, params, receiverCasted, args);
	}

	@Override
	public Object getField(String cn, String name, String fieldClassname, Object receiverRef)
	{
		return getField_(n2c(cn), name, n2c(fieldClassname), receiverRef);
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object getField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public void setField(String cn, String name, String fieldClassname, Object receiverRef, Object valueRef)
	{
		setField_(n2c(cn), name, n2c(fieldClassname), receiverRef, valueRef);
	}
	// extracted to method so the casts are expressible in Java
	private <T, F> void setField_(Class<T> clazz, String name, Class<F> fieldClass, Object receiver, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setField(clazz, name, fieldClass, receiverCasted, valueCasted);
	}
}
