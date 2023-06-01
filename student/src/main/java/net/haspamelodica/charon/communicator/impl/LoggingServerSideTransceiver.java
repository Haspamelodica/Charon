package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.ServerSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;

public class LoggingServerSideTransceiver<REF> implements ServerSideTransceiver<REF>
{
	private final CommunicationLogger<?, ?>		logger;
	private final ServerSideTransceiver<REF>	transceiver;

	public LoggingServerSideTransceiver(CommunicationLogger<?, ?> logger, ServerSideTransceiver<REF> transceiver)
	{
		this.logger = logger;
		this.transceiver = transceiver;
	}

	@Override
	public REF send(REF serdesRef, DataInput objIn) throws IOException
	{
		logger.logEnter("send with " + serdesRef + " from " + objIn);
		REF result = transceiver.send(serdesRef, objIn);
		logger.logExit(result);
		return result;
	}
	@Override
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException
	{
		logger.logEnter("receive " + objRef + " with " + serdesRef + " to " + objOut);
		transceiver.receive(serdesRef, objRef, objOut);
		logger.logExit();
	}
}
