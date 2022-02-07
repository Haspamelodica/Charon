package net.haspamelodica.studentcodeseparator.communicator.impl;

import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class LoggingCommunicator<ATTACHMENT, REF extends Ref<ATTACHMENT>>
		extends LoggingCommunicatorWithoutSerialization<ATTACHMENT, REF, StudentSideCommunicator<ATTACHMENT, REF>>
		implements StudentSideCommunicator<ATTACHMENT, REF>
{
	public LoggingCommunicator(StudentSideCommunicator<ATTACHMENT, REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicator(StudentSideCommunicator<ATTACHMENT, REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator, prefix);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator);
		return communicator;
	}

	@Override
	public <T> REF send(Serializer<T> serializer, REF serializerRef, T obj)
	{
		log("send " + serializer.getHandledClass() + ": " + serializer + ", " + serializerRef + ", " + obj);
		return communicator.send(serializer, serializerRef, obj);
	}
	@Override
	public <T> T receive(Serializer<T> serializer, REF serializerRef, REF objRef)
	{
		log("recv " + serializer.getHandledClass() + ": " + serializer + ", " + serializerRef + ", " + objRef);
		return communicator.receive(serializer, serializerRef, objRef);
	}
}
