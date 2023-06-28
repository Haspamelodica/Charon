package net.haspamelodica.charon.communicator.impl.logging;

import net.haspamelodica.charon.communicator.ExternalCallbackManager;

public class LoggingExternalCallbackManager<REF> implements ExternalCallbackManager<REF>
{
	private final CommunicationLogger<?, ?, ?, ?, ?>	logger;
	private final ExternalCallbackManager<REF>			callbackManager;

	public LoggingExternalCallbackManager(CommunicationLogger<?, ?, ?, ?, ?> logger, ExternalCallbackManager<REF> callbackManager)
	{
		this.logger = logger;
		this.callbackManager = callbackManager;
	}

	@Override
	public void createCallbackInstance(REF callbackRef, String interfaceCn)
	{
		logger.logEnter("new callback " + callbackRef + " " + interfaceCn);
		callbackManager.createCallbackInstance(callbackRef, interfaceCn);
		logger.logExit();
	}
}
