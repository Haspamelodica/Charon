package net.haspamelodica.charon.communicator.impl.logging;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class LoggingRefTranslatorCommunicatorSupplier<REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
		implements RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
{
	private final CommunicationLoggerParams										loggerParams;
	private final RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>		communicatorSupplier;
	private final BiFunction<CommunicationLogger<REF_TO, REF_TO>, TC_TO, TC_TO>	wrapTransceiverLogging;
	private final BiFunction<CommunicationLogger<REF_TO, REF_TO>, CM_TO, CM_TO>	wrapCallbackManagerLogging;

	public LoggingRefTranslatorCommunicatorSupplier(CommunicationLoggerParams loggerParams,
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO> communicatorSupplier,
			BiFunction<CommunicationLogger<REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging,
			BiFunction<CommunicationLogger<REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		this.loggerParams = loggerParams;
		this.communicatorSupplier = communicatorSupplier;
		this.wrapTransceiverLogging = wrapTransceiverLogging;
		this.wrapCallbackManagerLogging = wrapCallbackManagerLogging;
	}

	@Override
	public StudentSideCommunicator<REF_TO, REF_TO, ? extends TC_TO, ? extends CM_TO> createCommunicator(
			boolean storeRefsIdentityBased,
			StudentSideCommunicatorCallbacks<REF_TO, REF_TO> callbacks,
			RefTranslatorCommunicatorCallbacks<REF_TO> refTranslatorCommunicatorCallbacks)
	{
		return new LoggingCommunicator<REF_TO, REF_TO, TC_TO, CM_TO>(loggerParams,
				logger -> communicatorSupplier.createCommunicator(
						storeRefsIdentityBased,
						new LoggingStudentSideCommunicatorCallbacks<>(logger, callbacks),
						new LoggingRefTranslatorCommunicatorCallbacks<>(logger, refTranslatorCommunicatorCallbacks)),
				(logger, communicator) -> wrapTransceiverLogging.apply(logger, communicator.getTransceiver()),
				(logger, communicator) -> wrapCallbackManagerLogging.apply(logger, communicator.getCallbackManager()));
	}
}
