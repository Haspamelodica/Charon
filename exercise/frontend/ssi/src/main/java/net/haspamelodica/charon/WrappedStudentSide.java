package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.ClientSideCommunicatorUtils.maybeWrapLoggingIntClient;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapTypeCaching;

import java.io.IOException;

import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.impl.StudentSideImpl;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedStudentSide implements AutoCloseable
{
	private final WrappedCommunicator	communicator;
	private final StudentSide			studentSide;

	public WrappedStudentSide(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(Communication.open(CommunicationArgsParser.parse(args)));
	}

	public WrappedStudentSide(Communication communication)
	{
		WrappedCommunicator communicator = new WrappedCommunicator(communication);
		this.communicator = communicator;
		this.studentSide = new StudentSideImpl<>(
				wrapTypeCaching(
						maybeWrapLoggingIntClient(communication.getLogging(), CommunicationLoggerParams.DEFAULT,
								communicator.getClient())));
	}

	public StudentSide getStudentSide()
	{
		return studentSide;
	}

	@Override
	public void close() throws IOException
	{
		communicator.close();
	}
}
