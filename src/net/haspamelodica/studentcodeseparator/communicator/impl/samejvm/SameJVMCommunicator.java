package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import static net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRef.pack;
import static net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRef.unpack;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.nameToClass;

import java.util.List;

import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;

public class SameJVMCommunicator extends AbstractSameJVMCommunicator
{
	@Override
	public SameJVMRef callConstructor(String cn, List<String> params, List<SameJVMRef> argRefs)
	{
		return pack(ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), unpack(argRefs)));
	}

	@Override
	public SameJVMRef callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<SameJVMRef> argRefs)
	{
		return pack(ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params), unpack(argRefs)));
	}

	@Override
	public SameJVMRef getStaticField(String cn, String name, String fieldClassname)
	{
		return pack(ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname)));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, SameJVMRef valueRef)
	{
		setStaticField_(nameToClass(cn), name, nameToClass(fieldClassname), unpack(valueRef));
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public SameJVMRef callInstanceMethod(String cn, String name, String returnClassname, List<String> params, SameJVMRef receiverRef, List<SameJVMRef> argRefs)
	{
		return pack(callInstanceMethod_(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params), unpack(receiverRef), unpack(argRefs)));
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object callInstanceMethod_(Class<T> clazz, String name, Class<?> returnClass, List<Class<?>> params, Object receiver, List<Object> args)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.callInstanceMethod(clazz, name, returnClass, params, receiverCasted, args);
	}

	@Override
	public SameJVMRef getInstanceField(String cn, String name, String fieldClassname, SameJVMRef receiverRef)
	{
		return pack(getInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), unpack(receiverRef)));
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object getInstanceField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getInstanceField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, SameJVMRef receiverRef, SameJVMRef valueRef)
	{
		setInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), unpack(receiverRef), unpack(valueRef));
	}
	// extracted to method so the casts are expressible in Java
	private <T, F> void setInstanceField_(Class<T> clazz, String name, Class<F> fieldClass, Object receiver, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setInstanceField(clazz, name, fieldClass, receiverCasted, valueCasted);
	}
}
