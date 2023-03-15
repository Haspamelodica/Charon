package net.haspamelodica.charon.communicator;

import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public interface StudentSideCommunicatorClientSide<REF> extends StudentSideCommunicator<REF>
{
	public <T> REF send(REF serdesRef, Serializer<T> serializer, T obj);
	public <T> T receive(REF serdesRef, Deserializer<T> deserializer, REF objRef);
}
