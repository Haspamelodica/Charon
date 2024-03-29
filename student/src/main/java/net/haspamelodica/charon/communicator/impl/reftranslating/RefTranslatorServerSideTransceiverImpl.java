package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.ServerSideTransceiver;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class RefTranslatorServerSideTransceiverImpl<
		REF_TO,
		REF_FROM,
		TC_FROM extends ServerSideTransceiver<REF_FROM>>
		extends RefTranslatorTransceiverImpl<REF_TO, REF_FROM, TC_FROM>
		implements ServerSideTransceiver<REF_TO>
{
	public RefTranslatorServerSideTransceiverImpl(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ? extends TC_FROM,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		super(communicator, translator);
	}

	@Override
	public REF_TO send(REF_TO serdesRef, DataInput objIn) throws IOException
	{
		return translator.translateTo(communicator.getTransceiver().send(translator.translateFrom(serdesRef), objIn));
	}
	@Override
	public void receive(REF_TO serdesRef, REF_TO objRef, DataOutput objOut) throws IOException
	{
		communicator.getTransceiver().receive(translator.translateFrom(serdesRef), translator.translateFrom(objRef), objOut);
	}

	public static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					ServerSideTransceiver<REF_FROM>, RefTranslatorCommunicatorCallbacks<REF_TO>, ServerSideTransceiver<REF_TO>>
			supplier()
	{
		return RefTranslatorServerSideTransceiverImpl::create;
	}

	public static <REF_TO, REF_FROM>
			ServerSideTransceiver<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?,
					? extends ServerSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator,
					RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorServerSideTransceiverImpl<>(communicator, translator);
	}
}
