package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.refs.Ref;

public class LoggingCommunicatorClientSide<REF extends Ref<?, ?>>
		extends LoggingCommunicator<REF, StudentSideCommunicatorClientSide<REF>>
		implements StudentSideCommunicatorClientSide<REF>
{
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide<REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <REF extends Ref<?, ?>> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide<>(communicator, prefix);
		return communicator;
	}
	public static <REF extends Ref<?, ?>> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide<>(communicator);
		return communicator;
	}

	@Override
	public <T> REF send(REF serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		log("send " + serdesRef + ", " + sendObj + ", " + obj);
		return communicator.send(serdesRef, sendObj, obj);
	}
	@Override
	public <T> T receive(REF serdesRef, IOFunction<DataInput, T> receiveObj, REF objRef)
	{
		log("send " + serdesRef + ", " + receiveObj + ", " + objRef);
		return communicator.receive(serdesRef, receiveObj, objRef);
	}
}
