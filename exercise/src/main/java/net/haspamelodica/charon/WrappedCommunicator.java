package net.haspamelodica.charon;

import java.io.IOException;

import net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedCommunicator<REF extends Ref<Integer, ?>> implements AutoCloseable
{
	private final Communication					communication;
	private final DataCommunicatorClient<REF>	client;

	public WrappedCommunicator(String... args) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(Communication.open(CommunicationArgsParser.parse(args)));
	}

	public WrappedCommunicator(Communication communication)
	{
		this.communication = communication;
		this.client = new DataCommunicatorClient<>(communication.getIn(), communication.getOut());
	}

	public DataCommunicatorClient<REF> getClient()
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
