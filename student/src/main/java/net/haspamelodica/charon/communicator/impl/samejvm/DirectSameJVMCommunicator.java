package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClass;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.refs.direct.DirectRefManager;

public class DirectSameJVMCommunicator<REF extends Ref<Object, ?>>
		implements StudentSideCommunicatorServerSide<REF>
{
	protected final DirectRefManager<REF> refManager;

	public DirectSameJVMCommunicator(DirectRefManager<REF> refManager)
	{
		this.refManager = refManager;
	}

	@Override
	public String getStudentSideClassname(REF ref)
	{
		return classToName(refManager.unpack(ref).getClass());
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		return refManager.pack(ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), refManager.unpack(argRefs)));
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<REF> argRefs)
	{
		return refManager.pack(ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				refManager.unpack(argRefs)));
	}

	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		return refManager.pack(ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname)));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
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
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			REF receiverRef, List<REF> argRefs)
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
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef)
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
			REF receiverRef, REF valueRef)
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

	@Override
	public REF createCallbackInstance(String interfaceName, Callback<REF> callback)
	{
		//TODO create callback instance
		return null;
	}

	@Override
	public REF send(REF serdesRef, DataInput objIn) throws IOException
	{
		SerDes<?> serdes = (SerDes<?>) refManager.unpack(serdesRef);
		Object result = serdes.deserialize(objIn);
		return refManager.pack(result);
	}
	@Override
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException
	{
		SerDes<?> serdes = (SerDes<?>) refManager.unpack(serdesRef);
		Object obj = refManager.unpack(objRef);
		respondReceive(objOut, serdes, obj);
	}

	// extracted to own method so cast to T is expressible in Java
	private <T> void respondReceive(DataOutput out, SerDes<T> serdes, Object obj) throws IOException
	{
		@SuppressWarnings("unchecked") // responsibility of server
		T objCasted = (T) obj;
		serdes.serialize(out, objCasted);
	}
}
