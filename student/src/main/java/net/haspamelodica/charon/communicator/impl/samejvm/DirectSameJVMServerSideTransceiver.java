package net.haspamelodica.charon.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.ServerSideTransceiver;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.SerDes;

public class DirectSameJVMServerSideTransceiver extends DirectSameJVMTransceiverImpl implements ServerSideTransceiver<Object>
{
	public DirectSameJVMServerSideTransceiver(StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks)
	{
		super(callbacks);
	}

	@Override
	public Object send(Object serdesRef, DataInput objIn) throws IOException
	{
		SerDes<?> serdes = (SerDes<?>) serdesRef;
		return serdes.deserialize(objIn);
	}
	@Override
	public void receive(Object serdesRef, Object objRef, DataOutput objOut) throws IOException
	{
		SerDes<?> serdes = (SerDes<?>) serdesRef;
		Object obj = objRef;
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
