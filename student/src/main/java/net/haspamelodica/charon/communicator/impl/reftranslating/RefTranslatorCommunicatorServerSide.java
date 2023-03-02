package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;

public class RefTranslatorCommunicatorServerSide<REF_TO, REF_FROM>
		extends RefTranslatorCommunicator<REF_TO, REF_FROM, StudentSideCommunicatorServerSide<REF_FROM>>
		implements StudentSideCommunicatorServerSide<REF_TO>
{
	public RefTranslatorCommunicatorServerSide(StudentSideCommunicatorServerSide<REF_FROM> communicator, boolean storeRefsIdentityBased,
			RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		super(communicator, storeRefsIdentityBased, callbacks);
	}

	@Override
	public REF_TO send(REF_TO serdesRef, DataInput objIn) throws IOException
	{
		return translator.translateTo(communicator.send(translator.translateFrom(serdesRef), objIn));
	}
	@Override
	public void receive(REF_TO serdesRef, REF_TO objRef, DataOutput objOut) throws IOException
	{
		communicator.receive(translator.translateFrom(serdesRef), translator.translateFrom(objRef), objOut);
	}
}
