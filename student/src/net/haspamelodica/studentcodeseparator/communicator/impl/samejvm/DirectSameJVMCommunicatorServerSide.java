package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.refs.direct.DirectRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DirectSameJVMCommunicatorServerSide<REF extends Ref<Object, ?, ?, ?, ?, ?>>
		extends DirectSameJVMCommunicator<REF>
		implements StudentSideCommunicatorServerSide<REF>
{
	public DirectSameJVMCommunicatorServerSide(DirectRefManager<REF> refManager)
	{
		super(refManager);
	}

	@Override
	public REF send(REF serializerRef, DataInput objIn) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(serializerRef);
		Object result = serializer.deserialize(objIn);
		return refManager.pack(result);
	}
	@Override
	public void receive(REF serializerRef, REF objRef, DataOutput objOut) throws IOException
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
