package net.haspamelodica.charon.communicator;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.logging.LoggingCommunicator;
import net.haspamelodica.charon.communicator.impl.logging.LoggingExternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingInternalCallbackManager;
import net.haspamelodica.charon.communicator.impl.logging.LoggingRefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.logging.LoggingRefTranslatorCommunicatorCallbacksWithCreateBackwardRef;
import net.haspamelodica.charon.communicator.impl.logging.LoggingRefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.logging.LoggingUninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacksWithCreateBackwardRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorPartSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplierImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorExternalCallbackManagerImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorInternalCallbackManagerImpl;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.typecaching.TypeCachingCommunicator;

public class CommunicatorUtils
{
	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM,
			TYPEREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver,
			CM_TO extends CallbackManager,
			CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>
			wrapTypeCaching(RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS> supplier)
	{
		return (storeRefsIdentityBased, callbacks, refTranslatorCommunicatorCallbacks) -> new TypeCachingCommunicator<>(
				supplier.createCommunicator(storeRefsIdentityBased, callbacks, refTranslatorCommunicatorCallbacks));
	}
	public static <REF,
			THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
			wrapTypeCaching(UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					TC, CM> communicator)
	{
		return callbacks -> wrapTypeCaching(communicator.initialize(callbacks));
	}
	public static <REF,
			THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>
			wrapTypeCaching(StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends TC, ? extends CM> communicator)
	{
		return new TypeCachingCommunicator<>(communicator);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			UninitializedStudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, TC_TO, CM_TO>
			withReftransParamsFunctional(
					boolean storeRefsIdentityBased,
					Function<UntranslatedRef<?, ?>, REF_TO> createForwardRef,
					Function<UntranslatedRef<?, ?>, REF_TO> createBackwardRef,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> supplier)
	{
		return withReftransParams(storeRefsIdentityBased,
				RefTranslatorCommunicatorCallbacksWithCreateBackwardRef.fromFunctional(
						createForwardRef, createBackwardRef),
				supplier);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager>
			UninitializedStudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, TC_TO, CM_TO>
			withReftransParamsFunctional(
					boolean storeRefsIdentityBased,
					Function<UntranslatedRef<?, ?>, REF_TO> createForwardRef,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO,
							RefTranslatorCommunicatorCallbacks<REF_TO>> supplier)
	{
		return withReftransParams(storeRefsIdentityBased,
				RefTranslatorCommunicatorCallbacks.fromFunctional(createForwardRef), supplier);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager,
			CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			UninitializedStudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, TC_TO, CM_TO>
			withReftransParams(
					boolean storeRefsIdentityBased,
					CALLBACKS refTranslatorCommunicatorCallbacks,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS> supplier)
	{
		return supplier.withReftransParameters(storeRefsIdentityBased, refTranslatorCommunicatorCallbacks);
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver, CALLBACKS extends RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>, CALLBACKS>
			wrapReftransInt(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							TC_FROM, ? super CALLBACKS, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmInt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>, CALLBACKS>
			wrapReftransExt(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							TC_FROM, ? super CALLBACKS, TC_TO> createTransceiver)
	{
		return wrapReftrans(communicator, createTransceiver, reftransCmExt());
	}

	public static <
			REF_TO,
			TC_TO extends Transceiver,
			CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>
			wrapReftrans(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							TC_FROM, ? super CALLBACKS, TC_TO> createTransceiver,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							TC_FROM, ? super CALLBACKS, CM_TO> createCallbackManager)
	{
		return new RefTranslatorCommunicatorSupplierImpl<>(communicator, createTransceiver, createCallbackManager);
	}

	private static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					TC_FROM, RefTranslatorCommunicatorCallbacks<REF_TO>, ExternalCallbackManager<REF_TO>>
			reftransCmExt()
	{
		return RefTranslatorExternalCallbackManagerImpl.supplier();
	}
	private static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					TC_FROM, RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>, InternalCallbackManager<REF_TO>>
			reftransCmInt()
	{
		return RefTranslatorInternalCallbackManagerImpl.supplier();
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends TC, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					TC, InternalCallbackManager<REF>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingInt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			maybeWrapLoggingInt(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingInt(loggerParams, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends TC, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					TC, ExternalCallbackManager<REF>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExt(loggerParams, communicator, wrapTransceiverLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>, RefTranslatorCommunicatorCallbacks<REF_TO>>
			maybeWrapLoggingExt(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExt(loggerParams, communicatorSupplier, wrapTransceiverLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager,
			CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>
			maybeWrapLogging(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends TC, ? extends InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					TC, InternalCallbackManager<REF>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							TC, InternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmInt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			wrapLoggingInt(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, loggingCmInt(), loggingRefCbInt());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends TC, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					TC, ExternalCallbackManager<REF>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							TC, ExternalCallbackManager<REF>> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicator, wrapTransceiverLogging, loggingCmExt());
	}

	public static <REF_TO, TC_TO extends Transceiver>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>, RefTranslatorCommunicatorCallbacks<REF_TO>>
			wrapLoggingExt(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, TC_TO, TC_TO> wrapTransceiverLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, wrapTransceiverLogging, loggingCmExt(), loggingRefCbExt());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>
			wrapLogging(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return new LoggingCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>(loggerParams,
				logger -> communicator,
				(logger, communicator2) -> wrapTransceiverLogging.apply(logger, communicator2.getTransceiver()),
				(logger, communicator2) -> wrapCallbackManagerLogging.apply(logger, communicator2.getCallbackManager()));
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			TC extends Transceiver, CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
			wrapLogging(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return new LoggingUninitializedStudentSideCommunicator<>(loggerParams, communicator, wrapTransceiverLogging, wrapCallbackManagerLogging);
	}

	public static <REF_TO, TC_TO extends Transceiver, CM_TO extends CallbackManager,
			CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS>
			wrapLogging(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
							TC_TO, TC_TO> wrapTransceiverLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
							CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
							CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		return new LoggingRefTranslatorCommunicatorSupplier<>(loggerParams, communicatorSupplier,
				wrapTransceiverLogging, wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	private static <REF_TO> BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
			RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>,
			RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			loggingRefCbInt()
	{
		return LoggingRefTranslatorCommunicatorCallbacksWithCreateBackwardRef::new;
	}

	private static <REF_TO> BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>,
			RefTranslatorCommunicatorCallbacks<REF_TO>,
			RefTranslatorCommunicatorCallbacks<REF_TO>>
			loggingRefCbExt()
	{
		return LoggingRefTranslatorCommunicatorCallbacks::new;
	}

	private static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>,
					InternalCallbackManager<REF>, InternalCallbackManager<REF>>
			loggingCmInt()
	{
		return LoggingInternalCallbackManager::new;
	}
	private static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>,
					ExternalCallbackManager<REF>, ExternalCallbackManager<REF>>
			loggingCmExt()
	{
		return LoggingExternalCallbackManager::new;
	}

	private CommunicatorUtils()
	{}
}
