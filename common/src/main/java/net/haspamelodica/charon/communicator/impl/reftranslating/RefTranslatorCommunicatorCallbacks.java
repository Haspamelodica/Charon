package net.haspamelodica.charon.communicator.impl.reftranslating;

public interface RefTranslatorCommunicatorCallbacks<REF_TO>
{
	public REF_TO createForwardRef(UntranslatedRef untranslatedRef);
}
