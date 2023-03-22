package net.haspamelodica.charon.communicator;

public interface ExternalCallbackManager<REF> extends CallbackManager
{
	public void createCallbackInstance(REF callbackRef, String interfaceCn);
}
