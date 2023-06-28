package net.haspamelodica.charon.communicator.impl.logging;

import net.haspamelodica.charon.communicator.ExternalCallbackManager;

public class LoggingExternalCallbackManager<REF> implements ExternalCallbackManager<REF>
{
	private final CommunicationLogger<REF, ?, ?, ?, ?>	logger;
	private final ExternalCallbackManager<REF>			callbackManager;

	public LoggingExternalCallbackManager(CommunicationLogger<REF, ?, ?, ?, ?> logger, ExternalCallbackManager<REF> callbackManager)
	{
		this.logger = logger;
		this.callbackManager = callbackManager;
	}

	@Override
	public void createCallbackInstance(REF callbackRef, String interfaceCn)
	{
		logger.logEnter("new callback " + r(callbackRef) + " " + interfaceCn);
		callbackManager.createCallbackInstance(callbackRef, interfaceCn);
		logger.logExit();
	}

	private String r(REF ref)
	{
		return logger.refToString(ref);
	}
}
