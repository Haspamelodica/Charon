package net.haspamelodica.charon.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.refs.direct.DirectRefManager;
import net.haspamelodica.charon.serialization.SerDes;

public class DirectSameJVMCommunicatorServerSide<REF extends Ref<Object, ?>>
		extends DirectSameJVMCommunicator<REF>
		implements StudentSideCommunicatorServerSide<REF>
{
	public DirectSameJVMCommunicatorServerSide(DirectRefManager<REF> refManager)
	{
		super(refManager);
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
