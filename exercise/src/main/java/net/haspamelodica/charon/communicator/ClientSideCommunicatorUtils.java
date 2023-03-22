package net.haspamelodica.charon.communicator;

import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLogging;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingInt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftrans;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransInt;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.impl.LoggingClientSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorClientSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class ClientSideCommunicatorUtils
{
	public static <
			REF_TO,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapReftransIntClient(
					UninitializedStudentSideCommunicator<REF_FROM, ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapReftransExtClient(
					UninitializedStudentSideCommunicator<REF_FROM, ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			wrapReftransClient(
					UninitializedStudentSideCommunicator<REF_FROM, ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends ClientSideTransceiver<REF_FROM>,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcClient(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM>
			BiFunction<StudentSideCommunicator<REF_FROM, ? extends ClientSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ClientSideTransceiver<REF_TO>>
			reftransTcClient()
	{
		return RefTranslatorClientSideTransceiverImpl.supplier();
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(logger, communicator);
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(logger, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntClient(logger, communicatorSupplier);
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(logger, communicator);
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(logger, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtClient(logger, communicatorSupplier);
	}

	public static <REF, CM extends CallbackManager> StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(logger, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(logger, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			maybeWrapLoggingClient(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingClient(logger, communicatorSupplier, wrapCallbackManagerLogging);
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(logger, communicator, loggingTcClient());
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(logger, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapLoggingIntClient(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(logger, communicatorSupplier, loggingTcClient());
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(logger, communicator, loggingTcClient());
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(logger, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapLoggingExtClient(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(logger, communicatorSupplier, loggingTcClient());
	}

	public static <REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends CM>
			wrapLoggingClient(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, CM>
			wrapLoggingClient(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			wrapLoggingClient(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicatorSupplier, loggingTcClient(), wrapCallbackManagerLogging);
	}

	private static <REF> BiFunction<CommunicationLogger, ClientSideTransceiver<REF>, ClientSideTransceiver<REF>> loggingTcClient()
	{
		return LoggingClientSideTransceiver::new;
	}

	private ClientSideCommunicatorUtils()
	{}
}
