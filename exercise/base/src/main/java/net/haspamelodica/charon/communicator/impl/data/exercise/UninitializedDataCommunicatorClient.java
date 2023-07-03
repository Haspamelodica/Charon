package net.haspamelodica.charon.communicator.impl.data.exercise;

import java.io.InputStream;
import java.io.OutputStream;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.exchanges.Exchange;

public class UninitializedDataCommunicatorClient
		implements UninitializedStudentSideCommunicator<LongRef, LongRef, LongRef, LongRef, LongRef, LongRef,
				ClientSideTransceiver<LongRef>, InternalCallbackManager<LongRef>>
{
	private final Exchange rawExchange;

	private DataCommunicatorClient client;

	public UninitializedDataCommunicatorClient(Exchange rawExchange)
	{
		this.rawExchange = rawExchange;
	}

	@Override
	public DataCommunicatorClient initialize(StudentSideCommunicatorCallbacks<LongRef, LongRef, LongRef> callbacks)
	{
		if(client != null)
			throw new IllegalStateException("Client already initialized");

		client = new DataCommunicatorClient(rawExchange, callbacks);
		return client;
	}

	public void shutdown()
	{
		if(client == null)
			throw new IllegalStateException("Client not yet initialized");

		client.shutdown();
	}
}
