package net.haspamelodica.studentcodeseparator.communicator;

import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public interface StudentSideCommunicator<ATTACHMENT, REF extends Ref<ATTACHMENT>>
		extends StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF>
{
	public <T> REF send(Serializer<T> serializer, REF serializerRef, T obj);
	public <T> T receive(Serializer<T> serializer, REF serializerRef, REF objRef);
}
