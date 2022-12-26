package net.haspamelodica.charon.communicator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicatorServerSide extends StudentSideCommunicator
{
	public Ref send(Ref serdesRef, DataInput objIn) throws IOException;
	public void receive(Ref serdesRef, Ref objRef, DataOutput objOut) throws IOException;
}
