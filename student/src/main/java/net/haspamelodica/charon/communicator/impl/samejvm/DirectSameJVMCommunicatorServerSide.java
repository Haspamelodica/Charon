package net.haspamelodica.charon.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.refs.direct.DirectRefManager;

public class DirectSameJVMCommunicatorServerSide
		extends DirectSameJVMCommunicator
		implements StudentSideCommunicatorServerSide
{
	public DirectSameJVMCommunicatorServerSide(DirectRefManager refManager)
	{
		super(refManager);
	}

	@Override
	public Ref send(Ref serdesRef, DataInput objIn) throws IOException
	{
		SerDes<?> serdes = (SerDes<?>) refManager.unpack(serdesRef);
		Object result = serdes.deserialize(objIn);
		return refManager.pack(result);
	}
	@Override
	public void receive(Ref serdesRef, Ref objRef, DataOutput objOut) throws IOException
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
