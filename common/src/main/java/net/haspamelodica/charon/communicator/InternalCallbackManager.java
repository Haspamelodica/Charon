package net.haspamelodica.charon.communicator;

public interface InternalCallbackManager<REF> extends CallbackManager
{
	//TODO this can fail, so it should be OperationOutcome
	public REF createCallbackInstance(String interfaceCn);
}
