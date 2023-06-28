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
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorClientSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacksWithCreateBackwardRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorPartSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class ClientSideCommunicatorUtils
{
	public static <
			REF_TO,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			wrapReftransIntClient(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacks<REF_TO>>
			wrapReftransExtClient(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcClient());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			wrapReftransClient(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ClientSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ClientSideTransceiver<REF_FROM>, CALLBACKS, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcClient(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					ClientSideTransceiver<REF_FROM>, RefTranslatorCommunicatorCallbacks<REF_TO>, ClientSideTransceiver<REF_TO>>
			reftransTcClient()
	{
		return RefTranslatorClientSideTransceiverImpl.supplier();
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(loggerParams, communicator);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntClient(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			maybeWrapLoggingIntClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntClient(loggerParams, communicatorSupplier);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(loggerParams, communicator);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtClient(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacks<REF_TO>>
			maybeWrapLoggingExtClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtClient(loggerParams, communicatorSupplier);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, CM>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingClient(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			maybeWrapLoggingClient(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingClient(loggerParams, communicatorSupplier, wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			wrapLoggingIntClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(loggerParams, communicatorSupplier, loggingTcClient());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcClient());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacks<REF_TO>>
			wrapLoggingExtClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(loggerParams, communicatorSupplier, loggingTcClient());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends CM>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ClientSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, CM>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcClient(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			wrapLoggingClient(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ClientSideTransceiver<REF_TO>, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, loggingTcClient(), wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	private static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>,
					ClientSideTransceiver<REF>, ClientSideTransceiver<REF>>
			loggingTcClient()
	{
		return LoggingClientSideTransceiver::new;
	}

	private ClientSideCommunicatorUtils()
	{}
}
