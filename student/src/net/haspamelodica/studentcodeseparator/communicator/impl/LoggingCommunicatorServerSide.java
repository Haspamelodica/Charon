package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class LoggingCommunicatorServerSide<ATTACHMENT, REF extends Ref<ATTACHMENT>>
		extends LoggingCommunicator<ATTACHMENT, REF, StudentSideCommunicatorServerSide<ATTACHMENT, REF>>
		implements StudentSideCommunicatorServerSide<ATTACHMENT, REF>
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<ATTACHMENT, REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<ATTACHMENT, REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicatorServerSide<ATTACHMENT, REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<ATTACHMENT, REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide<>(communicator, prefix);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicatorServerSide<ATTACHMENT, REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<ATTACHMENT, REF> communicator, boolean logging)
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
