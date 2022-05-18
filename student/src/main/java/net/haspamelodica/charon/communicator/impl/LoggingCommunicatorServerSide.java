package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
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
	public REF send(REF serdesRef, DataInput objIn) throws IOException
	{
		log("send " + serdesRef + ", " + objIn);
		return communicator.send(serdesRef, objIn);
	}
	@Override
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException
	{
		log("receive " + serdesRef + ", " + objRef + ", " + objOut);
		communicator.receive(serdesRef, objRef, objOut);
	}
}
