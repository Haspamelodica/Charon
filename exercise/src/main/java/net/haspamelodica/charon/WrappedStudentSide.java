package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.impl.LoggingCommunicatorClientSide.maybeWrapLoggingC;

import java.io.IOException;

import net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.charon.impl.StudentSideImpl;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedStudentSide implements AutoCloseable
{
	private final Communication	communication;
	DataCommunicatorClient<?>	client;
	private final StudentSide	studentSide;

	public WrappedStudentSide(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(Communication.open(CommunicationArgsParser.parse(args)));
	}

	public WrappedStudentSide(Communication communication)
	{
		this.communication = communication;
		DataCommunicatorClient<Ref<Integer, Object>> client = new DataCommunicatorClient<>(communication.getIn(), communication.getOut());
		this.client = client;
		this.studentSide = new StudentSideImpl<>(maybeWrapLoggingC(client, communication.getLogging()));
	}

	public StudentSide getStudentSide()
	{
		return studentSide;
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			client.shutdown();
		} finally
		{
			communication.close();
		}
	}
}
