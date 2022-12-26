package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.refs.Ref;

public class LoggingCommunicatorClientSide extends LoggingCommunicator<StudentSideCommunicatorClientSide> implements StudentSideCommunicatorClientSide
{
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static StudentSideCommunicatorClientSide maybeWrapLoggingC(StudentSideCommunicatorClientSide communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide(communicator, prefix);
		return communicator;
	}
	public static StudentSideCommunicatorClientSide maybeWrapLoggingC(StudentSideCommunicatorClientSide communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide(communicator);
		return communicator;
	}

	@Override
	public <T> Ref send(Ref serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		log("send " + serdesRef + ", " + sendObj + ", " + obj);
		return communicator.send(serdesRef, sendObj, obj);
	}
	@Override
	public <T> T receive(Ref serdesRef, IOFunction<DataInput, T> receiveObj, Ref objRef)
	{
		log("send " + serdesRef + ", " + receiveObj + ", " + objRef);
		return communicator.receive(serdesRef, receiveObj, objRef);
	}
}
