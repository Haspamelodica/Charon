package net.haspamelodica.charon.communicator;

import java.io.IOException;

import net.haspamelodica.charon.communicator.impl.data.exercise.UninitializedDataCommunicatorClient;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class CloseableDataCommunicatorClient implements AutoCloseable
{
	private final Communication							communication;
	private final UninitializedDataCommunicatorClient	client;

	public CloseableDataCommunicatorClient(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(Communication.open(CommunicationArgsParser.parse(args)));
	}

	public CloseableDataCommunicatorClient(Communication communication)
	{
		this.communication = communication;
		this.client = new UninitializedDataCommunicatorClient(communication.getIn(), communication.getOut());
	}

	public UninitializedStudentSideCommunicator<LongRef, LongRef, LongRef, LongRef, LongRef, LongRef,
			ClientSideTransceiver<LongRef>, InternalCallbackManager<LongRef>> getClient()
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
