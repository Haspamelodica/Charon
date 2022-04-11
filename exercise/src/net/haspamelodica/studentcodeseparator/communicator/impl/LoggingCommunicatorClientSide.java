package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class LoggingCommunicatorClientSide<REF extends Ref<?, ?, ?, ?, ?, ?>>
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

	public static <REF extends Ref<?, ?, ?, ?, ?, ?>> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide<>(communicator, prefix);
		return communicator;
	}
	public static <REF extends Ref<?, ?, ?, ?, ?, ?>> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide<>(communicator);
		return communicator;
	}

	@Override
	public <T> REF send(REF serializerRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		log("send " + serializerRef + ", " + sendObj + ", " + obj);
		return communicator.send(serializerRef, sendObj, obj);
	}
	@Override
	public <T> T receive(REF serializerRef, IOFunction<DataInput, T> receiveObj, REF objRef)
	{
		log("send " + serializerRef + ", " + receiveObj + ", " + objRef);
		return communicator.receive(serializerRef, receiveObj, objRef);
	}
}
