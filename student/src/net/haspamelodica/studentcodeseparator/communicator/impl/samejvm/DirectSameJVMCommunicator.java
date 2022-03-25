package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.nameToClass;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.Callback;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.refs.direct.DirectRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DirectSameJVMCommunicator<REFERRER>
		implements StudentSideCommunicatorServerSide<Object, REFERRER, Ref<Object, REFERRER>>
{
	protected final DirectRefManager<REFERRER> refManager;

	public DirectSameJVMCommunicator(DirectRefManager<REFERRER> refManager)
	{
		this.refManager = refManager;
	}

	@Override
	public String getStudentSideClassname(Ref<Object, REFERRER> ref)
	{
		return classToName(refManager.unpack(ref).getClass());
	}

	@Override
	public Ref<Object, REFERRER> callConstructor(String cn, List<String> params, List<Ref<Object, REFERRER>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), refManager.unpack(argRefs)));
	}

	@Override
	public Ref<Object, REFERRER> callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<Ref<Object, REFERRER>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				refManager.unpack(argRefs)));
	}

	@Override
	public Ref<Object, REFERRER> getStaticField(String cn, String name, String fieldClassname)
	{
		return refManager.pack(ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname)));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, Ref<Object, REFERRER> valueRef)
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
	public Ref<Object, REFERRER> callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			Ref<Object, REFERRER> receiverRef, List<Ref<Object, REFERRER>> argRefs)
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
	public Ref<Object, REFERRER> getInstanceField(String cn, String name, String fieldClassname, Ref<Object, REFERRER> receiverRef)
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
			Ref<Object, REFERRER> receiverRef, Ref<Object, REFERRER> valueRef)
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
	public Ref<Object, REFERRER> createCallbackInstance(String interfaceName, Callback<Object, REFERRER, Ref<Object, REFERRER>> callback)
	{
		//TODO create callback instance
		return null;
	}

	@Override
	public Ref<Object, REFERRER> send(Ref<Object, REFERRER> serializerRef, DataInput objIn) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(serializerRef);
		Object result = serializer.deserialize(objIn);
		return refManager.pack(result);
	}
	@Override
	public void receive(Ref<Object, REFERRER> serializerRef, Ref<Object, REFERRER> objRef, DataOutput objOut) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(serializerRef);
		Object obj = refManager.unpack(objRef);
		respondReceive(objOut, serializer, obj);
	}

	// extracted to own method so cast to T is expressible in Java
	private <T> void respondReceive(DataOutput out, Serializer<T> serializer, Object obj) throws IOException
	{
		@SuppressWarnings("unchecked") // responsibility of server
		T objCasted = (T) obj;
		serializer.serialize(out, objCasted);
	}
}
