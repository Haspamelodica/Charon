package net.haspamelodica.charon.communicator;

import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLogging;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapLoggingInt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftrans;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransExt;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapReftransInt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.haspamelodica.charon.communicator.impl.LoggingServerSideTransceiver;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacksWithCreateBackwardRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorPartSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorServerSideTransceiverImpl;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicator;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMServerSideTransceiver;

public class ServerSideCommunicatorUtils
{
	public static StudentSideCommunicator<Object, Throwable, Class<?>, Constructor<?>, Method, Field,
			? extends ServerSideTransceiver<Object>, ? extends InternalCallbackManager<Object>>
			createDirectCommServer(ClassLoader studentClassesClassloader,
					StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks)
	{
		return new DirectSameJVMCommunicator<>(studentClassesClassloader, callbacks, directTcServer());
	}

	public static UninitializedStudentSideCommunicator<Object, Throwable, Class<?>, Constructor<?>, Method, Field,
			ServerSideTransceiver<Object>, InternalCallbackManager<Object>>
			createDirectCommServer(ClassLoader studentClassesClassloader)
	{
		return DirectSameJVMCommunicator.createUninitializedCommunicator(studentClassesClassloader, directTcServer());
	}

	private static Function<StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>>, ServerSideTransceiver<Object>> directTcServer()
	{
		return DirectSameJVMServerSideTransceiver::new;
	}

	public static <
			REF_TO,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			wrapReftransIntServer(UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
					CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransInt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>,
					ExternalCallbackManager<REF_TO>, RefTranslatorCommunicatorCallbacks<REF_TO>>
			wrapReftransExtServer(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator)
	{
		return wrapReftransExt(communicator, reftransTcServer());
	}

	public static <
			REF_TO,
			CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>,
			REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			wrapReftransServer(
					UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
							CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ServerSideTransceiver<REF_FROM>, InternalCallbackManager<REF_FROM>> communicator,
					RefTranslatorCommunicatorPartSupplier<REF_TO,
							REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
							ServerSideTransceiver<REF_FROM>, CALLBACKS, CM_TO> createCallbackManager)
	{
		return wrapReftrans(communicator, reftransTcServer(), createCallbackManager);
	}

	private static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					ServerSideTransceiver<REF_FROM>, RefTranslatorCommunicatorCallbacks<REF_TO>, ServerSideTransceiver<REF_TO>>
			reftransTcServer()
	{
		return RefTranslatorServerSideTransceiverImpl.supplier();
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(loggerParams, communicator);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingIntServer(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			maybeWrapLoggingIntServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingIntServer(loggerParams, communicatorSupplier);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(loggerParams, communicator);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		if(!logging)
			return communicator;
		return wrapLoggingExtServer(loggerParams, communicator);
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacks<REF_TO>>
			maybeWrapLoggingExtServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingExtServer(loggerParams, communicatorSupplier);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, CM>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		if(!logging)
			return communicator;
		return wrapLoggingServer(loggerParams, communicator, wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			maybeWrapLoggingServer(boolean logging, CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		if(!logging)
			return communicatorSupplier;
		return wrapLoggingServer(loggerParams, communicatorSupplier, wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, InternalCallbackManager<REF>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, InternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingInt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>>
			wrapLoggingIntServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, InternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingInt(loggerParams, communicatorSupplier, loggingTcServer());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, ExternalCallbackManager<REF>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, ExternalCallbackManager<REF>> communicator)
	{
		return wrapLoggingExt(loggerParams, communicator, loggingTcServer());
	}

	public static <REF_TO>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
					RefTranslatorCommunicatorCallbacks<REF_TO>>
			wrapLoggingExtServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, ExternalCallbackManager<REF_TO>,
							RefTranslatorCommunicatorCallbacks<REF_TO>> communicatorSupplier)
	{
		return wrapLoggingExt(loggerParams, communicatorSupplier, loggingTcServer());
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ServerSideTransceiver<REF>, ? extends CM>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends ServerSideTransceiver<REF>, ? extends CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
			CM extends CallbackManager>
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ServerSideTransceiver<REF>, CM>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ServerSideTransceiver<REF>, CM> communicator,
					BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		return wrapLogging(loggerParams, communicator, loggingTcServer(), wrapCallbackManagerLogging);
	}

	public static <REF_TO, CM_TO extends CallbackManager, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
			RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO, CALLBACKS>
			wrapLoggingServer(CommunicationLoggerParams loggerParams,
					RefTranslatorCommunicatorSupplier<REF_TO, ServerSideTransceiver<REF_TO>, CM_TO, CALLBACKS> communicatorSupplier,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CM_TO, CM_TO> wrapCallbackManagerLogging,
					BiFunction<CommunicationLogger<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO>, CALLBACKS, CALLBACKS> wrapCallbacksLogging)
	{
		return wrapLogging(loggerParams, communicatorSupplier, loggingTcServer(), wrapCallbackManagerLogging, wrapCallbacksLogging);
	}

	private static <REF, THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>,
					ServerSideTransceiver<REF>, ServerSideTransceiver<REF>>
			loggingTcServer()
	{
		return LoggingServerSideTransceiver::new;
	}

	private ServerSideCommunicatorUtils()
	{}
}
