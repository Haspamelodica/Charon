package net.haspamelodica.charon.communicator.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;

public class RefTranslatorCommunicatorServerSide<REF_TO, REF_FROM>
		extends RefTranslatorCommunicator<REF_TO, REF_FROM, StudentSideCommunicatorServerSide<REF_FROM>>
		implements StudentSideCommunicatorServerSide<REF_TO>
{
	public RefTranslatorCommunicatorServerSide(StudentSideCommunicatorServerSide<REF_FROM> communicator, boolean storeRefsIdentityBased)
	{
		super(communicator, storeRefsIdentityBased);
	}

	@Override
	public REF_TO send(REF_TO serdesRef, DataInput objIn) throws IOException
	{
		return translateTo(communicator.send(translateFrom(serdesRef), objIn));
	}
	@Override
	public void receive(REF_TO serdesRef, REF_TO objRef, DataOutput objOut) throws IOException
	{
		communicator.receive(translateFrom(serdesRef), translateFrom(objRef), objOut);
	}
}
