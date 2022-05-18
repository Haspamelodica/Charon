package net.haspamelodica.charon.communicator;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicatorClientSide<REF extends Ref<?, ?>>
		extends StudentSideCommunicator<REF>
{
	public <T> REF send(REF serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj);
	public <T> T receive(REF serdesRef, IOFunction<DataInput, T> receiveObj, REF objRef);
}
