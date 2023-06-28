package net.haspamelodica.charon.communicator.impl.logging;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class LoggingRefTranslatorCommunicatorSupplier<REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager,
		CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
		implements RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>
{
	private final CommunicationLoggerParams												loggerParams;
	private final RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>	communicatorSupplier;

	private final BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
			TC_TO, TC_TO> wrapTransceiverLogging;

	private final BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
			CM_TO, CM_TO> wrapCallbackManagerLogging;

	private final BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
			CALLBACKS, CALLBACKS> wrapCallbacksLogging;

	public LoggingRefTranslatorCommunicatorSupplier(CommunicationLoggerParams loggerParams,
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS> communicatorSupplier,
			BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging,
			BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
			BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		this.loggerParams = loggerParams;
		this.communicatorSupplier = communicatorSupplier;
		this.wrapTransceiverLogging = wrapTransceiverLogging;
		this.wrapCallbackManagerLogging = wrapCallbackManagerLogging;
		this.wrapCallbacksLogging = wrapCallbacksLogging;
	}

	@Override
	public StudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, ? extends TC_TO, ? extends CM_TO>
			createCommunicator(
					boolean storeRefsIdentityBased,
					StudentSideCommunicatorCallbacks<REF_TO, REF_TO, REF_TO> callbacks,
					CALLBACKS refTranslatorCommunicatorCallbacks)
	{
		return new LoggingCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, TC_TO, CM_TO>(loggerParams,
				logger -> communicatorSupplier.createCommunicator(
						storeRefsIdentityBased,
						new LoggingStudentSideCommunicatorCallbacks<>(logger, callbacks),
						wrapCallbacksLogging.apply(logger, refTranslatorCommunicatorCallbacks)),
				(logger, communicator) -> wrapTransceiverLogging.apply(logger, communicator.getTransceiver()),
				(logger, communicator) -> wrapCallbackManagerLogging.apply(logger, communicator.getCallbackManager()));
	}
}
