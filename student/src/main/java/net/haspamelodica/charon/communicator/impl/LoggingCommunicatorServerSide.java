package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorServerSideSupplier;

public class LoggingCommunicatorServerSide<REF>
		extends LoggingCommunicator<REF, StudentSideCommunicatorServerSide<REF>>
		implements StudentSideCommunicatorServerSide<REF>
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REF> communicator)
	{
		super(communicator);
	}
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}

	public static <REF> StudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REF> communicator, boolean logging)
	{
		return maybeWrapLoggingS(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> StudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(StudentSideCommunicatorServerSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicatorServerSide<>(communicator, prefix);
		return communicator;
	}
	public static RefTranslatorCommunicatorServerSideSupplier maybeWrapLoggingS(
			RefTranslatorCommunicatorServerSideSupplier communicatorSupplier, boolean logging)
	{
		return maybeWrapLoggingS(communicatorSupplier, DEFAULT_PREFIX, logging);
	}
	public static RefTranslatorCommunicatorServerSideSupplier maybeWrapLoggingS(
			RefTranslatorCommunicatorServerSideSupplier communicatorSupplier, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingRefTranslatorCommunicatorServerSideSupplier(communicatorSupplier, prefix);
		return communicatorSupplier;
	}

	@Override
	public REF send(REF serdesRef, DataInput objIn) throws IOException
	{
		log("send " + serdesRef + ", " + objIn);
		return communicator.send(serdesRef, objIn);
	}
	@Override
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException
	{
		log("receive " + serdesRef + ", " + objRef + ", " + objOut);
		communicator.receive(serdesRef, objRef, objOut);
	}

	private static class LoggingRefTranslatorCommunicatorServerSideSupplier
			extends LoggingRefTranslatorCommunicatorSupplier<RefTranslatorCommunicatorServerSideSupplier>
			implements RefTranslatorCommunicatorServerSideSupplier
	{
		protected LoggingRefTranslatorCommunicatorServerSideSupplier(RefTranslatorCommunicatorServerSideSupplier communicatorSupplier, String prefix)
		{
			super(communicatorSupplier, prefix);
		}

		@Override
		public <REF_TO> StudentSideCommunicatorServerSide<REF_TO> createCommunicator(
				boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
		{
			return new LoggingCommunicatorServerSide<>(communicatorSupplier.createCommunicator(storeRefsIdentityBased, callbacks), prefix);
		}
	}
}
