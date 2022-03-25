package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class LoggingCommunicatorServerSide<REFERENT, REFERRER, REF extends Ref<REFERENT, REFERRER>>
		extends LoggingCommunicator<REFERENT, REFERRER, REF, StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF>>
		implements StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF>
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <REFERENT, REFERRER, REF extends Ref<REFERENT, REFERRER>> StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide<>(communicator, prefix);
		return communicator;
	}
	public static <REFERENT, REFERRER, REF extends Ref<REFERENT, REFERRER>> StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REFERENT, REFERRER, REF> communicator, boolean logging)
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
