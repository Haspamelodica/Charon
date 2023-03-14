package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClass;

import java.util.List;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class DirectSameJVMCommunicator implements StudentSideCommunicator<Object>
{
	private final StudentSideCommunicatorCallbacks<Object> callbacks;

	public DirectSameJVMCommunicator(StudentSideCommunicatorCallbacks<Object> callbacks)
	{
		this.callbacks = callbacks;
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return true;
	}

	@Override
	public String getClassname(Object ref)
	{
		return classToName(ref.getClass());
	}

	@Override
	public String getSuperclass(String cn)
	{
		return classToName(nameToClass(cn).getSuperclass());
	}

	@Override
	public List<String> getInterfaces(String cn)
	{
		return classToName(List.of(nameToClass(cn).getInterfaces()));
	}

	@Override
	public Object callConstructor(String cn, List<String> params, List<Object> argRefs)
	{
		return ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), argRefs);
	}

	@Override
	public Object callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<Object> argRefs)
	{
		return ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params), argRefs);
	}

	@Override
	public Object getStaticField(String cn, String name, String fieldClassname)
	{
		return ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, Object valueRef)
	{
		setStaticField_(nameToClass(cn), name, nameToClass(fieldClassname), valueRef);
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public Object callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			Object receiverRef, List<Object> argRefs)
	{
		return callInstanceMethod_(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				receiverRef, argRefs);
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
	public Object getInstanceField(String cn, String name, String fieldClassname, Object receiverRef)
	{
		return getInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), receiverRef);
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
			Object receiverRef, Object valueRef)
	{
		setInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), receiverRef, valueRef);
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

	@Override
	public Object createCallbackInstance(String interfaceCn)
	{
		return createProxyInstance(nameToClass(interfaceCn), (proxy, method, args) -> callbacks.callCallbackInstanceMethod(
				interfaceCn, method.getName(), classToName(method.getReturnType()), classToName(method.getParameterTypes()), proxy, argsToList(args)));
	}
}
