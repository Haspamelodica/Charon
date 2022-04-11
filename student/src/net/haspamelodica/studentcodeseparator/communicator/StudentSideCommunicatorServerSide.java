package net.haspamelodica.studentcodeseparator.communicator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface StudentSideCommunicatorServerSide<REF extends Ref<?, ?, ?, ?, ?, ?>> extends StudentSideCommunicator<REF>
{
	public REF send(REF serializerRef, DataInput objIn) throws IOException;
	public void receive(REF serializerRef, REF objRef, DataOutput objOut) throws IOException;
}
