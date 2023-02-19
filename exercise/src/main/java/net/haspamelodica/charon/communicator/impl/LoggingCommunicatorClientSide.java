package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorClientSideSupplier;

public class LoggingCommunicatorClientSide<REF>
		extends LoggingCommunicator<REF, StudentSideCommunicatorClientSide<REF>>
		implements StudentSideCommunicatorClientSide<REF>
{
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide<REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorClientSide(StudentSideCommunicatorClientSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <REF>
			StudentSideCommunicatorClientSide<REF> maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, boolean logging)
	{
		return maybeWrapLoggingC(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF>
			StudentSideCommunicatorClientSide<REF> maybeWrapLoggingC(StudentSideCommunicatorClientSide<REF> communicator, String prefix, boolean logging)
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
	public <T> REF send(REF serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		log("send " + serdesRef + ", " + sendObj + ", " + obj);
		return communicator.send(serdesRef, sendObj, obj);
	}
	@Override
	public <T> T receive(REF serdesRef, IOFunction<DataInput, T> receiveObj, REF objRef)
	{
		log("send " + serdesRef + ", " + receiveObj + ", " + objRef);
		return communicator.receive(serdesRef, receiveObj, objRef);
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
