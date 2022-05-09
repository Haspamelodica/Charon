package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.LoggingCommunicator;
import net.haspamelodica.charon.refs.Ref;

public class LoggingCommunicatorServerSide<REF extends Ref<?, ?>>
		extends LoggingCommunicator<REF, StudentSideCommunicatorServerSide<REF>>
		implements StudentSideCommunicatorServerSide<REF>
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <REF extends Ref<?, ?>> StudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide<>(communicator, prefix);
		return communicator;
	}
	public static <REF extends Ref<?, ?>> StudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REF> communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide<>(communicator);
		return communicator;
	}

	@Override
	public REF send(REF serializerRef, DataInput objIn) throws IOException
	{
		log("send " + serializerRef + ", " + objIn);
		return communicator.send(serializerRef, objIn);
	}
	@Override
	public void receive(REF serializerRef, REF objRef, DataOutput objOut) throws IOException
	{
		log("receive " + serializerRef + ", " + objRef + ", " + objOut);
		communicator.receive(serializerRef, objRef, objOut);
	}
}
