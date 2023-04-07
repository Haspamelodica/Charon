package net.haspamelodica.charon.communicator;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.logging.LoggingCommunicator;
import net.haspamelodica.charon.communicator.impl.logging.LoggingExternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingInternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingRefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.logging.LoggingUninitializedStudentSideCommunicator;
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
			TYPEREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			wrapReftransInt(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmInt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			wrapReftransExt(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmExt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			CM_TO extends CallbackManager,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			wrapReftrans(
					UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver,
					BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
							? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		return new RefTranslatorCommunicatorSupplierImpl<>(communicator, createTransceiver, createCallbackManager);
	}

	private static <REF_TO, REF_FROM, TYPEREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ExternalCallbackManager<REF_TO>>
			reftransCmExt()
	{
		return RefTranslatorExternalCallbackManagerImpl.supplier();
	}
	private static <REF_TO, REF_FROM, TYPEREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, InternalCallbackManager<REF_TO>>
			reftransCmInt()
	{
		return RefTranslatorInternalCallbackManagerImpl.supplier();
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingInt(loggerParams, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExt(loggerParams, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>
			wrapLogging(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return new LoggingCommunicator<REF, TYPEREF, TC, CM>(loggerParams,
				logger -> communicator,
				(logger, communicator2) -> wrapTransceiverLogging.apply(logger, communicator2.getTransceiver()),
				(logger, communicator2) -> wrapCallbackManagerLogging.apply(logger, communicator2.getCallbackManager()));
	}

	public static <REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM>
			wrapLogging(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM> communicator,
					BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return new LoggingUninitializedStudentSideCommunicator<>(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
			wrapLogging(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging)
	{
		return new LoggingRefTranslatorCommunicatorSupplier<>(loggerParams, communicatorSupplier, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	private static <REF, TYPEREF extends REF>
			BiFunction<CommunicationLogger<TYPEREF>, InternalCallbackManager<REF>, InternalCallbackManager<REF>> loggingCmInt()
	{
		return LoggingInternalCallbackManager::new;
	}
	private static <REF, TYPEREF extends REF>
			BiFunction<CommunicationLogger<TYPEREF>, ExternalCallbackManager<REF>, ExternalCallbackManager<REF>> loggingCmExt()
	{
		return LoggingExternalCallbackManager::new;
	}

	private CommunicatorUtils()
	{}
}
