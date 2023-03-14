package net.haspamelodica.charon;

import java.io.IOException;

import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.UninitializedDataCommunicatorClient;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedCommunicator implements AutoCloseable
{
	private final Communication							communication;
	private final UninitializedDataCommunicatorClient	client;

	public WrappedCommunicator(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(Communication.open(CommunicationArgsParser.parse(args)));
	}

	public WrappedCommunicator(Communication communication)
	{
		this.communication = communication;
		this.client = new UninitializedDataCommunicatorClient(communication.getIn(), communication.getOut());
	}

	public UninitializedStudentSideCommunicatorClientSide<LongRef> getClient()
	{
		return client;
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
