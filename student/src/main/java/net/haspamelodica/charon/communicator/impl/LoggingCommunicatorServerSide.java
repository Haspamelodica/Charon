package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.refs.Ref;

public class LoggingCommunicatorServerSide
		extends LoggingCommunicator<StudentSideCommunicatorServerSide>
		implements StudentSideCommunicatorServerSide
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static StudentSideCommunicatorServerSide
			maybeWrapLoggingS(StudentSideCommunicatorServerSide communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide(communicator, prefix);
		return communicator;
	}
	public static StudentSideCommunicatorServerSide
			maybeWrapLoggingS(StudentSideCommunicatorServerSide communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide(communicator);
		return communicator;
	}

	@Override
	public Ref send(Ref serdesRef, DataInput objIn) throws IOException
	{
		log("send " + serdesRef + ", " + objIn);
		return communicator.send(serdesRef, objIn);
	}
	@Override
	public void receive(Ref serdesRef, Ref objRef, DataOutput objOut) throws IOException
	{
		log("receive " + serdesRef + ", " + objRef + ", " + objOut);
		communicator.receive(serdesRef, objRef, objOut);
	}
}
