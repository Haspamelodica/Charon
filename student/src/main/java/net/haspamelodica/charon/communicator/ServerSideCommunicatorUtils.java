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
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorServerSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicator;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMServerSideTransceiver;

public class ServerSideCommunicatorUtils
{
	public static StudentSideCommunicator<Object, ? extends ServerSideTransceiver<Object>, ? extends InternalCallbackManager<Object>>
			createDirectCommServer(StudentSideCommunicatorCallbacks<Object> callbacks)
	{
		return new DirectSameJVMCommunicator<>(callbacks, directTcServer());
	}

	public static UninitializedStudentSideCommunicator<Object, ServerSideTransceiver<Object>, InternalCallbackManager<Object>>
			createDirectCommServer()
	{
		return DirectSameJVMCommunicator.createUninitializedCommunicator(directTcServer());
	}

	private static Function<StudentSideCommunicatorCallbacks<Object>, ServerSideTransceiver<Object>> directTcServer()
	{
		return DirectSameJVMServerSideTransceiver::new;
	}

	public static <
			REF_TO,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapReftransIntServer(
					UninitializedStudentSideCommunicator<REF_FROM, ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapReftransExtServer(
					UninitializedStudentSideCommunicator<REF_FROM, ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager,
			REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			wrapReftransServer(
					UninitializedStudentSideCommunicator<REF_FROM, ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends ServerSideTransceiver<REF_FROM>,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcServer(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM>
			BiFunction<StudentSideCommunicator<REF_FROM, ? extends ServerSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ServerSideTransceiver<REF_TO>>
			reftransTcServer()
	{
		return RefTranslatorServerSideTransceiverImpl.supplier();
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(logger, communicator);
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(logger, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntServer(logger, communicatorSupplier);
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(logger, communicator);
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(logger, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtServer(logger, communicatorSupplier);
	}

	public static <REF, CM extends CallbackManager> StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(logger, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(logger, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			maybeWrapLoggingServer(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingServer(logger, communicatorSupplier, wrapCallbackManagerLogging);
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(logger, communicator, loggingTcServer());
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(logger, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>>
			wrapLoggingIntServer(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(logger, communicatorSupplier, loggingTcServer());
	}

	public static <REF>
			StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(logger, communicator, loggingTcServer());
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(logger, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>>
			wrapLoggingExtServer(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(logger, communicatorSupplier, loggingTcServer());
	}

	public static <REF, CM extends CallbackManager>
			StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends CM>
			wrapLoggingServer(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, CM>
			wrapLoggingServer(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO>
			wrapLoggingServer(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return wrapLogging(logger, communicatorSupplier, loggingTcServer(), wrapCallbackManagerLogging);
	}

	private static <REF> BiFunction<CommunicationLogger, ServerSideTransceiver<REF>, ServerSideTransceiver<REF>> loggingTcServer()
	{
		return LoggingServerSideTransceiver::new;
	}

	private ServerSideCommunicatorUtils()
	{}
}
