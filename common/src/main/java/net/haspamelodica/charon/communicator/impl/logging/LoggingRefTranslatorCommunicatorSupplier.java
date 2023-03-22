package net.haspamelodica.charon.communicator.impl.logging;

import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLogging;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class LoggingRefTranslatorCommunicatorSupplier<
		REF_TO,
		TC_TO extends Transceiver,
		CM_TO extends CallbackManager>
		implements RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
{
	private final CommunicationLogger															logger;
	private final RefTranslatorCommunicatorSupplier<REF_TO, ? extends TC_TO, ? extends CM_TO>	communicatorSupplier;
	private final BiFunction<CommunicationLogger, TC_TO, TC_TO>									wrapTransceiverLogging;
	private final BiFunction<CommunicationLogger, CM_TO, CM_TO>									wrapCallbackManagerLogging;

	public LoggingRefTranslatorCommunicatorSupplier(CommunicationLogger logger,
			RefTranslatorCommunicatorSupplier<REF_TO, ? extends TC_TO, ? extends CM_TO> communicatorSupplier,
			BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging,
			BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		this.logger = logger;
		this.communicatorSupplier = communicatorSupplier;
		this.wrapTransceiverLogging = wrapTransceiverLogging;
		this.wrapCallbackManagerLogging = wrapCallbackManagerLogging;
	}

	@Override
	public StudentSideCommunicator<REF_TO, ? extends TC_TO, ? extends CM_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		StudentSideCommunicator<REF_TO, ? extends TC_TO, ? extends CM_TO> communicator = communicatorSupplier.createCommunicator(
				storeRefsIdentityBased, new LoggingRefTranslatorCommunicatorCallbacks<>(logger, callbacks));
		return wrapLogging(logger, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}
}
