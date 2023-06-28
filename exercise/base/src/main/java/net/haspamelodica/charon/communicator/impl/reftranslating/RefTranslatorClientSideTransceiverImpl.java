package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public class RefTranslatorClientSideTransceiverImpl<
		REF_TO,
		REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
		CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
		TC_FROM extends ClientSideTransceiver<REF_FROM>>
		extends RefTranslatorTransceiverImpl<REF_TO, REF_FROM, TC_FROM>
		implements ClientSideTransceiver<REF_TO>
{
	public RefTranslatorClientSideTransceiverImpl(StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
			CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, ? extends TC_FROM,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		super(communicator, translator);
	}

	@Override
	public <T> REF_TO send(REF_TO serdesRef, Serializer<T> serializer, T obj)
	{
		return translator.translateTo(communicator.getTransceiver().send(translator.translateFrom(serdesRef), serializer, obj));
	}
	@Override
	public <T> T receive(REF_TO serdesRef, Deserializer<T> deserializer, REF_TO objRef)
	{
		return communicator.getTransceiver().receive(translator.translateFrom(serdesRef), deserializer, translator.translateFrom(objRef));
	}

	public static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					ClientSideTransceiver<REF_FROM>, RefTranslatorCommunicatorCallbacks<REF_TO>, ClientSideTransceiver<REF_TO>>
			supplier()
	{
		return RefTranslatorClientSideTransceiverImpl::create;
	}

	public static <REF_TO, REF_FROM>
			ClientSideTransceiver<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?,
					? extends ClientSideTransceiver<REF_FROM>, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator,
					RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorClientSideTransceiverImpl<>(communicator, translator);
	}
}
