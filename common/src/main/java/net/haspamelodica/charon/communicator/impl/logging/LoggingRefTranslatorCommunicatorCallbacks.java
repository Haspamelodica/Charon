package net.haspamelodica.charon.communicator.impl.logging;

import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;

public class LoggingRefTranslatorCommunicatorCallbacks<REF> implements RefTranslatorCommunicatorCallbacks<REF>
{
	private final RefTranslatorCommunicatorCallbacks<REF> callbacks;

	public LoggingRefTranslatorCommunicatorCallbacks(CommunicationLogger<REF, REF> logger, RefTranslatorCommunicatorCallbacks<REF> callbacks)
	{
		this.callbacks = callbacks;
	}

	@Override
	public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF createForwardRef(UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef)
	{
		// No need to log this
		return callbacks.createForwardRef(untranslatedRef);
	}
}
