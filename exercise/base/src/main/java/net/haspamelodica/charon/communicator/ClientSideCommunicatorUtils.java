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
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorClientSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class ClientSideCommunicatorUtils
{
	public static <
			REF_TO,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapReftransIntClient(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapReftransExtClient(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			wrapReftransClient(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends ClientSideTransceiver<REF_FROM>,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcClient(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM, TYPEREF_FROM extends REF_FROM>
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM,
					? extends ClientSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ClientSideTransceiver<REF_TO>>
			reftransTcClient()
	{
		return RefTranslatorClientSideTransceiverImpl.supplier();
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(loggerParams, communicator);
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntClient(loggerParams, communicatorSupplier);
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(loggerParams, communicator);
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtClient(loggerParams, communicatorSupplier);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingClient(loggerParams, communicatorSupplier, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(loggerParams, communicatorSupplier, loggingTcClient());
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(loggerParams, communicatorSupplier, loggingTcClient());
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends CM>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, CM>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, loggingTcClient(), wrapCallbackManagerLogging);
	}

	private static <REF, TYPEREF extends REF>
			BiFunction<CommunicationLogger<REF, TYPEREF>, ClientSideTransceiver<REF>, ClientSideTransceiver<REF>> loggingTcClient()
	{
		return LoggingClientSideTransceiver::new;
	}

	private ClientSideCommunicatorUtils()
	{}
}
