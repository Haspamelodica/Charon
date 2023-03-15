package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;

public class RefTranslatorCommunicatorClientSide<REF_TO, REF_FROM>
		extends RefTranslatorCommunicator<REF_TO, REF_FROM, StudentSideCommunicatorClientSide<REF_FROM>>
		implements StudentSideCommunicatorClientSide<REF_TO>
{
	public RefTranslatorCommunicatorClientSide(UninitializedStudentSideCommunicator<REF_FROM, StudentSideCommunicatorClientSide<REF_FROM>> communicator,
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		super(communicator, storeRefsIdentityBased, callbacks);
	}

	@Override
	public <T> REF_TO send(REF_TO serdesRef, Serializer<T> serializer, T obj)
	{
		return translator.translateTo(communicator.send(translator.translateFrom(serdesRef), serializer, obj));
	}
	@Override
	public <T> T receive(REF_TO serdesRef, Deserializer<T> deserializer, REF_TO objRef)
	{
		return communicator.receive(translator.translateFrom(serdesRef), deserializer, translator.translateFrom(objRef));
	}
}
