package net.haspamelodica.charon.communicator.impl;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public class LoggingClientSideTransceiver<REF> implements ClientSideTransceiver<REF>
{
	private final CommunicationLogger<REF, ?, ?, ?, ?>	logger;
	private final ClientSideTransceiver<REF>			transceiver;

	public LoggingClientSideTransceiver(CommunicationLogger<REF, ?, ?, ?, ?> logger, ClientSideTransceiver<REF> transceiver)
	{
		this.logger = logger;
		this.transceiver = transceiver;
	}

	@Override
	public <T> REF send(REF serdesRef, Serializer<T> serializer, T obj)
	{
		logger.logEnter("send " + o(obj) + " with " + r(serdesRef) + " / " + serializer);
		REF result = transceiver.send(serdesRef, serializer, obj);
		logger.logExit(r(result));
		return result;
	}
	@Override
	public <T> T receive(REF serdesRef, Deserializer<T> deserializer, REF objRef)
	{
		logger.logEnter("receive " + r(objRef) + " with " + r(serdesRef) + " / " + deserializer);
		T result = transceiver.receive(serdesRef, deserializer, objRef);
		logger.logExit(o(result));
		return result;
	}

	private String o(Object object)
	{
		return logger.objectToString(object);
	}

	private String r(REF ref)
	{
		return logger.refToString(ref);
	}
}
