package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.nameToClass;

import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;

public class DirectSameJVMCommunicatorWithoutSerialization<ATTACHMENT>
		implements StudentSideCommunicatorWithoutSerialization<ATTACHMENT, SameJVMRef<ATTACHMENT>>
{
	protected final SameJVMRefManager<ATTACHMENT> refManager;

	public DirectSameJVMCommunicatorWithoutSerialization(SameJVMRefManager<ATTACHMENT> refManager)
	{
		this.refManager = refManager;
	}

	@Override
	public String getStudentSideClassname(SameJVMRef<ATTACHMENT> ref)
	{
		return classToName(refManager.unpack(ref).getClass());
	}

	@Override
	public SameJVMRef<ATTACHMENT> callConstructor(String cn, List<String> params, List<SameJVMRef<ATTACHMENT>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), refManager.unpack(argRefs)));
	}

	@Override
	public SameJVMRef<ATTACHMENT> callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<SameJVMRef<ATTACHMENT>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				refManager.unpack(argRefs)));
	}

	@Override
	public SameJVMRef<ATTACHMENT> getStaticField(String cn, String name, String fieldClassname)
	{
		return refManager.pack(ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname)));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, SameJVMRef<ATTACHMENT> valueRef)
	{
		setStaticField_(nameToClass(cn), name, nameToClass(fieldClassname), refManager.unpack(valueRef));
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public SameJVMRef<ATTACHMENT> callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			SameJVMRef<ATTACHMENT> receiverRef, List<SameJVMRef<ATTACHMENT>> argRefs)
	{
		return refManager.pack(callInstanceMethod_(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				refManager.unpack(receiverRef), refManager.unpack(argRefs)));
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object callInstanceMethod_(Class<T> clazz, String name, Class<?> returnClass, List<Class<?>> params,
			Object receiver, List<Object> args)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.callInstanceMethod(clazz, name, returnClass, params, receiverCasted, args);
	}

	@Override
	public SameJVMRef<ATTACHMENT> getInstanceField(String cn, String name, String fieldClassname, SameJVMRef<ATTACHMENT> receiverRef)
	{
		return refManager.pack(getInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), refManager.unpack(receiverRef)));
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object getInstanceField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getInstanceField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public void setInstanceField(String cn, String name, String fieldClassname,
			SameJVMRef<ATTACHMENT> receiverRef, SameJVMRef<ATTACHMENT> valueRef)
	{
		setInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname),
				refManager.unpack(receiverRef), refManager.unpack(valueRef));
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
