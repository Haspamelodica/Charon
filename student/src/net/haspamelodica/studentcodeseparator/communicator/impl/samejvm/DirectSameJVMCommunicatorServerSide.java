package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.DirectRef;
import net.haspamelodica.studentcodeseparator.refs.DirectRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DirectSameJVMCommunicatorServerSide<ATTACHMENT>
		extends DirectSameJVMCommunicator<ATTACHMENT>
		implements StudentSideCommunicatorServerSide<ATTACHMENT, DirectRef<ATTACHMENT>>
{
	public DirectSameJVMCommunicatorServerSide(DirectRefManager<ATTACHMENT> refManager)
	{
		super(refManager);
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
