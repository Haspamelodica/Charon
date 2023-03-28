package net.haspamelodica.charon.communicator.impl;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public class LoggingClientSideTransceiver<REF> implements ClientSideTransceiver<REF>
{
	private final CommunicationLogger			logger;
	private final ClientSideTransceiver<REF>	transceiver;

	public LoggingClientSideTransceiver(CommunicationLogger logger, ClientSideTransceiver<REF> transceiver)
	{
		this.logger = logger;
		this.transceiver = transceiver;
	}

	@Override
	public <T> REF send(REF serdesRef, Serializer<T> serializer, T obj)
	{
		logger.logEnter("send " + obj + " with " + serdesRef + " / " + serializer);
		REF result = transceiver.send(serdesRef, serializer, obj);
		logger.logExit(result);
		return result;
	}
	@Override
	public <T> T receive(REF serdesRef, Deserializer<T> deserializer, REF objRef)
	{
		logger.logEnter("receive " + objRef + " with " + serdesRef + " / " + deserializer);
		T result = transceiver.receive(serdesRef, deserializer, objRef);
		logger.logExit(result);
		return result;
	}
}
