package net.haspamelodica.studentcodeseparator;

import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorWithoutSerialization;

public class LoggingUtils
{
	private static final boolean LOGGING = true;

	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF> communicator, String prefix)
	{
		if(LOGGING)
			return new LoggingCommunicatorWithoutSerialization<>(communicator, prefix);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator, String prefix)
	{
		if(LOGGING)
			return new LoggingCommunicator<>(communicator, prefix);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF> communicator)
	{
		if(LOGGING)
			return new LoggingCommunicatorWithoutSerialization<>(communicator);
		return communicator;
	}
	public static <ATTACHMENT, REF extends Ref<ATTACHMENT>> StudentSideCommunicator<ATTACHMENT, REF>
			maybeWrapLogging(StudentSideCommunicator<ATTACHMENT, REF> communicator)
	{
		if(LOGGING)
			return new LoggingCommunicator<>(communicator);
		return communicator;
	}

	private LoggingUtils()
	{}
}
