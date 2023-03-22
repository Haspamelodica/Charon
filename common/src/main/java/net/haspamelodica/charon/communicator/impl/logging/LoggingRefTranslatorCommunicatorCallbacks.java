package net.haspamelodica.charon.communicator.impl.logging;

import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;

public class LoggingRefTranslatorCommunicatorCallbacks<REF>
		extends LoggingStudentSideCommunicatorCallbacks<REF, RefTranslatorCommunicatorCallbacks<REF>>
		implements RefTranslatorCommunicatorCallbacks<REF>
{
	public LoggingRefTranslatorCommunicatorCallbacks(CommunicationLogger logger, RefTranslatorCommunicatorCallbacks<REF> callbacks)
	{
		super(logger, callbacks);
	}

	@Override
	public <REF_FROM> REF createForwardRef(UntranslatedRef<REF_FROM> untranslatedRef)
	{
		return callbacks.createForwardRef(untranslatedRef);
	}
}

