package net.haspamelodica.charon.communicator;

import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLogging;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingInt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftrans;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransInt;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.haspamelodica.charon.communicator.impl.LoggingServerSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorServerSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicator;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMServerSideTransceiver;

public class ServerSideCommunicatorUtils
{
	public static StudentSideCommunicator<Object, Class<?>, ? extends ServerSideTransceiver<Object>, ? extends InternalCallbackManager<Object>>
			createDirectCommServer(StudentSideCommunicatorCallbacks<Object, Class<?>> callbacks)
	{
		return new DirectSameJVMCommunicator<>(callbacks, directTcServer());
	}

	public static UninitializedStudentSideCommunicator<Object, Class<?>, ServerSideTransceiver<Object>, InternalCallbackManager<Object>>
			createDirectCommServer()
	{
		return DirectSameJVMCommunicator.createUninitializedCommunicator(directTcServer());
	}

	private static Function<StudentSideCommunicatorCallbacks<Object, Class<?>>, ServerSideTransceiver<Object>> directTcServer()
	{
		return DirectSameJVMServerSideTransceiver::new;
	}

	public static <
			REF_TO,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapReftransIntServer(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapReftransExtServer(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			wrapReftransServer(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM,
							ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends ServerSideTransceiver<REF_FROM>,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcServer(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM, TYPEREF_FROM extends REF_FROM>
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM,
					? extends ServerSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ServerSideTransceiver<REF_TO>>
			reftransTcServer()
	{
		return RefTranslatorServerSideTransceiverImpl.supplier();
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(loggerParams, communicator);
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntServer(loggerParams, communicatorSupplier);
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(loggerParams, communicator);
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtServer(loggerParams, communicatorSupplier);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingServer(loggerParams, communicatorSupplier, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(loggerParams, communicatorSupplier, loggingTcServer());
	}

	public static <REF, TYPEREF extends REF>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF, TYPEREF extends REF>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(loggerParams, communicatorSupplier, loggingTcServer());
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends CM>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, CM>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, loggingTcServer(), wrapCallbackManagerLogging);
	}

	private static <REF, TYPEREF extends REF>
			BiFunction<CommunicationLogger<TYPEREF>, ServerSideTransceiver<REF>, ServerSideTransceiver<REF>> loggingTcServer()
	{
		return LoggingServerSideTransceiver::new;
	}

	private ServerSideCommunicatorUtils()
	{}
}
