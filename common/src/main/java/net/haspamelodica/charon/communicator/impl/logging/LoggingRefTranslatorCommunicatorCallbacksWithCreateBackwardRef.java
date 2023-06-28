package net.haspamelodica.charon.communicator.impl.logging;

import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacksWithCreateBackwardRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;

public class LoggingRefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF>
		extends LoggingRefTranslatorCommunicatorCallbacks<REF, RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF>>
		implements RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF>
{
	public LoggingRefTranslatorCommunicatorCallbacksWithCreateBackwardRef(
			CommunicationLogger<REF, REF, REF, REF, REF> logger,
			RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF> callbacks)
	{
		super(logger, callbacks);
	}

	@Override
	public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF createBackwardRef(UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef)
	{
		// No need to log this
		return callbacks.createBackwardRef(untranslatedRef);
	}
}
