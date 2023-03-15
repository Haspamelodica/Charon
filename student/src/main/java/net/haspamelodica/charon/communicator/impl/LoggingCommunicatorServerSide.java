package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorServerSideSupplier;

public class LoggingCommunicatorServerSide<REF>
		extends LoggingCommunicator<REF, StudentSideCommunicatorServerSide<REF>>
		implements StudentSideCommunicatorServerSide<REF>
{
	public LoggingCommunicatorServerSide(StudentSideCommunicatorServerSide<REF> communicator, String prefix)
	{
		super(communicator, prefix);
	}
	public LoggingCommunicatorServerSide(UninitializedStudentSideCommunicator<REF, StudentSideCommunicatorServerSide<REF>> communicator,
			StudentSideCommunicatorCallbacks<REF> callbacks, String prefix)
	{
		super(communicator, callbacks, prefix);
	}

	public static <REF> UninitializedStudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(UninitializedStudentSideCommunicatorServerSide<REF> communicator, boolean logging)
	{
		return maybeWrapLoggingS(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> UninitializedStudentSideCommunicatorServerSide<REF>
			maybeWrapLoggingS(UninitializedStudentSideCommunicatorServerSide<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return c -> new LoggingCommunicatorServerSide<>(communicator, c, prefix);
		return communicator;
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
		logEnter("send " + serdesRef + ", " + objIn);
		REF result = communicator.send(serdesRef, objIn);
		logExit(result);
		return result;
	}
	@Override
	public void receive(REF serdesRef, REF objRef, DataOutput objOut) throws IOException
	{
		logEnter("receive " + serdesRef + ", " + objRef + ", " + objOut);
		communicator.receive(serdesRef, objRef, objOut);
		logExit();
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
