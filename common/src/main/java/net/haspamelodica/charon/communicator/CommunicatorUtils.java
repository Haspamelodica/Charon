package net.haspamelodica.charon.communicator;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.logging.LoggingCommunicator;
import net.haspamelodica.charon.communicator.impl.logging.LoggingExternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingInternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingRefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.logging.LoggingStudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplierImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorExternalCallbackManagerImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorInternalCallbackManagerImpl;

public class CommunicatorUtils
{
	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			wrapReftransInt(
					UninitializedStudentSideCommunicator<REF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmInt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			wrapReftransExt(
					UninitializedStudentSideCommunicator<REF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmExt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			CM_TO extends CallbackManager,
			REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			wrapReftrans(
					UninitializedStudentSideCommunicator<REF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver,
					BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return new RefTranslatorCommunicatorSupplierImpl<>(communicator, createTransceiver, createCallbackManager);
	}

	private static <REF_TO, REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ExternalCallbackManager<REF_TO>>
			reftransCmExt()
	{
		return RefTranslatorExternalCallbackManagerImpl.supplier();
	}
	private static <REF_TO, REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, InternalCallbackManager<REF_TO>>
			reftransCmInt()
	{
		return RefTranslatorInternalCallbackManagerImpl.supplier();
	}

	public static <REF, TC extends Transceiver>
			StudentSideCommunicator<REF, ? extends TC, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(logger, communicator, wrapTransceiverLogging);
	}

	public static <REF, TC extends Transceiver> UninitializedStudentSideCommunicator<REF, TC, InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(logger, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingInt(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingInt(logger, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, TC extends Transceiver>
			StudentSideCommunicator<REF, ? extends TC, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(logger, communicator, wrapTransceiverLogging);
	}

	public static <REF, TC extends Transceiver> UninitializedStudentSideCommunicator<REF, TC, ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(logger, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExt(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExt(logger, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, TC extends Transceiver, CM extends CallbackManager> StudentSideCommunicator<REF, ? extends TC, ? extends CM>
			maybeWrapLogging(boolean logging, CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(logger, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF, TC extends Transceiver, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, TC, CM>
			maybeWrapLogging(boolean logging, CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, CM> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(logger, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			maybeWrapLogging(boolean logging, CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLogging(logger, communicatorSupplier, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF> StudentSideCommunicatorCallbacks<REF> maybeWrapLogging(boolean logging,
			CommunicationLogger logger, StudentSideCommunicatorCallbacks<REF> callbacks)
	{
		if(!logging)
			return callbacks;
		return wrapLogging(logger, callbacks);
	}

	public static <REF, TC extends Transceiver>
			StudentSideCommunicator<REF, ? extends TC, ? extends InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF, TC extends Transceiver> UninitializedStudentSideCommunicator<REF, TC, InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			wrapLoggingInt(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicatorSupplier, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF, TC extends Transceiver>
			StudentSideCommunicator<REF, ? extends TC, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF, TC extends Transceiver> UninitializedStudentSideCommunicator<REF, TC, ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			wrapLoggingExt(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(logger, communicatorSupplier, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF, TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, ? extends TC, ? extends CM>
			wrapLogging(CommunicationLogger logger,
					StudentSideCommunicator<REF, ? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return new LoggingCommunicator<REF, TC, CM>(logger, communicator,
				wrapTransceiverLogging.apply(logger, communicator.getTransceiver()),
				wrapCallbackManagerLogging.apply(logger, communicator.getCallbackManager()));
	}

	public static <REF, TC extends Transceiver, CM extends CallbackManager> UninitializedStudentSideCommunicator<REF, TC, CM>
			wrapLogging(CommunicationLogger logger,
					UninitializedStudentSideCommunicator<REF, TC, CM> communicator,
					BiFunction<CommunicationLogger, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM, CM> wrapCallbackManagerLogging)
	{
		return callbacks -> wrapLogging(
				logger,
				communicator.initialize(wrapLogging(logger, callbacks)),
				wrapTransceiverLogging,
				wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			wrapLogging(CommunicationLogger logger,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger, TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return new LoggingRefTranslatorCommunicatorSupplier<>(logger, communicatorSupplier, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF> StudentSideCommunicatorCallbacks<REF> wrapLogging(
			CommunicationLogger logger, StudentSideCommunicatorCallbacks<REF> callbacks)
	{
		return new LoggingStudentSideCommunicatorCallbacks<>(logger, callbacks);
	}

	private static <REF> BiFunction<CommunicationLogger, InternalCallbackManager<REF>, InternalCallbackManager<REF>> loggingCmInt()
	{
		return LoggingInternalCallbackManager::new;
	}
	private static <REF> BiFunction<CommunicationLogger, ExternalCallbackManager<REF>, ExternalCallbackManager<REF>> loggingCmExt()
	{
		return LoggingExternalCallbackManager::new;
	}

	private CommunicatorUtils()
	{}
}
