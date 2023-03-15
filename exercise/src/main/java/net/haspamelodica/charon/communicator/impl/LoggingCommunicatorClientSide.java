package net.haspamelodica.charon.communicator.impl;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorClientSideSupplier;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public class LoggingCommunicatorClientSide<REF>
		extends LoggingCommunicator<REF, StudentSideCommunicatorClientSide<REF>>
		implements StudentSideCommunicatorClientSide<REF>
{
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public LoggingCommunicatorClientSide(UninitializedStudentSideCommunicator<REF, StudentSideCommunicatorClientSide<REF>> communicator,
			StudentSideCommunicatorCallbacks<REF> callbacks, String prefix)
	{
		super(communicator, callbacks, prefix);
	}

	public static <REF> UninitializedStudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(UninitializedStudentSideCommunicatorClientSide<REF> communicator, boolean logging)
	{
		return maybeWrapLoggingC(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> UninitializedStudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(UninitializedStudentSideCommunicatorClientSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return c -> new LoggingCommunicatorClientSide<>(communicator, c, prefix);
		return communicator;
	}
	public static <REF> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, boolean logging)
	{
		return maybeWrapLoggingC(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> StudentSideCommunicatorClientSide<REF>
			maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorClientSide<>(communicator, prefix);
		return communicator;
	}
	public static RefTranslatorCommunicatorClientSideSupplier maybeWrapLoggingC(
			RefTranslatorCommunicatorClientSideSupplier communicatorSupplier, boolean logging)
	{
		return maybeWrapLoggingC(communicatorSupplier, DEFAULT_PREFIX, logging);
	}
	public static RefTranslatorCommunicatorClientSideSupplier maybeWrapLoggingC(
			RefTranslatorCommunicatorClientSideSupplier communicatorSupplier, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingRefTranslatorCommunicatorClientSideSupplier(communicatorSupplier, prefix);
		return communicatorSupplier;
	}

	@Override
	public <T> REF send(REF serdesRef, Serializer<T> serializer, T obj)
	{
		logEnter("send " + serdesRef + ", " + serializer + ", " + obj);
		REF result = communicator.send(serdesRef, serializer, obj);
		logExit(result);
		return result;
	}
	@Override
	public <T> T receive(REF serdesRef, Deserializer<T> deserializer, REF objRef)
	{
		logEnter("receive " + serdesRef + ", " + deserializer + ", " + objRef);
		T result = communicator.receive(serdesRef, deserializer, objRef);
		logExit(result);
		return result;
	}

	private static class LoggingRefTranslatorCommunicatorClientSideSupplier
			extends LoggingRefTranslatorCommunicatorSupplier<RefTranslatorCommunicatorClientSideSupplier>
			implements RefTranslatorCommunicatorClientSideSupplier
	{
		protected LoggingRefTranslatorCommunicatorClientSideSupplier(RefTranslatorCommunicatorClientSideSupplier communicatorSupplier, String prefix)
		{
			super(communicatorSupplier, prefix);
		}

		@Override
		public <REF_TO> StudentSideCommunicatorClientSide<REF_TO> createCommunicator(
				boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
		{
			return new LoggingCommunicatorClientSide<>(communicatorSupplier.createCommunicator(storeRefsIdentityBased, callbacks), prefix);
		}
	}
}
