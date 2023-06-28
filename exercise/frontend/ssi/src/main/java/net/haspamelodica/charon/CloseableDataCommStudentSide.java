package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.ClientSideCommunicatorUtils.maybeWrapLoggingIntClient;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapTypeCaching;

import java.io.IOException;

import net.haspamelodica.charon.communicator.CloseableDataCommunicatorClient;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.impl.StudentSideImpl;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.CommunicationParams;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class CloseableDataCommStudentSide implements CloseableStudentSide
{
	private final CloseableDataCommunicatorClient	communicator;
	private final StudentSide						studentSide;

	public CloseableDataCommStudentSide(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(CommunicationArgsParser.parse(args));
	}

	public CloseableDataCommStudentSide(CommunicationParams params) throws IOException, InterruptedException
	{
		this(Communication.open(params));
	}

	public CloseableDataCommStudentSide(Communication communication)
	{
		CloseableDataCommunicatorClient communicator = new CloseableDataCommunicatorClient(communication);
		this.communicator = communicator;
		this.studentSide = new StudentSideImpl<>(
				wrapTypeCaching(
						//TODO make ALL_TO_STRING optional
						maybeWrapLoggingIntClient(communication.getLogging(), CommunicationLoggerParams.DEFAULT_REF_TO_STRING,
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
