package net.haspamelodica.charon.communicator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface StudentSideCommunicatorServerSide<REF> extends StudentSideCommunicator<REF>
{
	public REF send(REF serdesRef, DataInput objIn) throws IOException;
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException;
}
