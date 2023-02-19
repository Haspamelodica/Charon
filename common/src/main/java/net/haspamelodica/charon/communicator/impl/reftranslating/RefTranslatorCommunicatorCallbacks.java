package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicator.UntranslatedRef;

public interface RefTranslatorCommunicatorCallbacks<REF_TO>
{
	public REF_TO createForwardRef(UntranslatedRef untranslatedRef);
}
