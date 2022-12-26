package net.haspamelodica.charon.communicator;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicatorClientSide extends StudentSideCommunicator
{
	public <T> Ref send(Ref serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj);
	public <T> T receive(Ref serdesRef, IOFunction<DataInput, T> receiveObj, Ref objRef);
}
