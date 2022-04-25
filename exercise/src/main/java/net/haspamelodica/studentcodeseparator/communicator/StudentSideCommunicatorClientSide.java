package net.haspamelodica.studentcodeseparator.communicator;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface StudentSideCommunicatorClientSide<REF extends Ref<?, ?>>
		extends StudentSideCommunicator<REF>
{
	public <T> REF send(REF serializerRef, IOBiConsumer<DataOutput, T> sendObj, T obj);
	public <T> T receive(REF serializerRef, IOFunction<DataInput, T> receiveObj, REF objRef);
}