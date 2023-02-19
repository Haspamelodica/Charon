package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;

@FunctionalInterface
public interface RefTranslatorCommunicatorSupplier
{
	public <REF_TO> StudentSideCommunicator<REF_TO> createCommunicator(boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks);
}
