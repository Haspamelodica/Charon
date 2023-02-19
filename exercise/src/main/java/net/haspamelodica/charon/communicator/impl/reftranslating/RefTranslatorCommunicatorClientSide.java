package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.io.DataInput;
import java.io.DataOutput;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;

public class RefTranslatorCommunicatorClientSide<REF_TO, REF_FROM>
		extends RefTranslatorCommunicator<REF_TO, REF_FROM, StudentSideCommunicatorClientSide<REF_FROM>>
		implements StudentSideCommunicatorClientSide<REF_TO>
{
	public RefTranslatorCommunicatorClientSide(StudentSideCommunicatorClientSide<REF_FROM> communicator, boolean storeRefsIdentityBased,
			RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		super(communicator, storeRefsIdentityBased, callbacks);
	}

	@Override
	public <T> REF_TO send(REF_TO serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		return translateTo(communicator.send(translateFrom(serdesRef), sendObj, obj));
	}
	@Override
	public <T> T receive(REF_TO serdesRef, IOFunction<DataInput, T> receiveObj, REF_TO objRef)
	{
		return communicator.receive(translateFrom(serdesRef), receiveObj, translateFrom(objRef));
	}
}
