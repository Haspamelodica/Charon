package net.haspamelodica.charon.communicator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicatorServerSide<REF extends Ref<?, ?>> extends StudentSideCommunicator<REF>
{
	public REF send(REF serializerRef, DataInput objIn) throws IOException;
	public void receive(REF serializerRef, REF objRef, DataOutput objOut) throws IOException;
}
