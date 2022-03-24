package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.nameToClass;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;
import net.haspamelodica.studentcodeseparator.refs.DirectRef;
import net.haspamelodica.studentcodeseparator.refs.DirectRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DirectSameJVMCommunicator<ATTACHMENT>
		implements StudentSideCommunicatorServerSide<ATTACHMENT, DirectRef<ATTACHMENT>>
{
	protected final DirectRefManager<ATTACHMENT> refManager;

	public DirectSameJVMCommunicator(DirectRefManager<ATTACHMENT> refManager)
	{
		this.refManager = refManager;
	}

	@Override
	public String getStudentSideClassname(DirectRef<ATTACHMENT> ref)
	{
		return classToName(refManager.unpack(ref).getClass());
	}

	@Override
	public DirectRef<ATTACHMENT> callConstructor(String cn, List<String> params, List<DirectRef<ATTACHMENT>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), refManager.unpack(argRefs)));
	}

	@Override
	public DirectRef<ATTACHMENT> callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<DirectRef<ATTACHMENT>> argRefs)
	{
		return refManager.pack(ReflectionUtils.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				refManager.unpack(argRefs)));
	}

	@Override
	public DirectRef<ATTACHMENT> getStaticField(String cn, String name, String fieldClassname)
	{
		return refManager.pack(ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname)));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, DirectRef<ATTACHMENT> valueRef)
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
	public DirectRef<ATTACHMENT> callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			DirectRef<ATTACHMENT> receiverRef, List<DirectRef<ATTACHMENT>> argRefs)
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
	public DirectRef<ATTACHMENT> getInstanceField(String cn, String name, String fieldClassname, DirectRef<ATTACHMENT> receiverRef)
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
			DirectRef<ATTACHMENT> receiverRef, DirectRef<ATTACHMENT> valueRef)
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
	public DirectRef<ATTACHMENT> send(DirectRef<ATTACHMENT> serializerRef, DataInput objIn) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(serializerRef);
		Object result = serializer.deserialize(objIn);
		return refManager.pack(result);
	}
	@Override
	public void receive(DirectRef<ATTACHMENT> serializerRef, DirectRef<ATTACHMENT> objRef, DataOutput objOut) throws IOException
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
