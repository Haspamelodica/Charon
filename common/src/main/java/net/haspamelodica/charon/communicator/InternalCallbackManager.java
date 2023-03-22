package net.haspamelodica.charon.communicator;

public interface InternalCallbackManager<REF> extends CallbackManager
{
	public REF createCallbackInstance(String interfaceCn);
}
