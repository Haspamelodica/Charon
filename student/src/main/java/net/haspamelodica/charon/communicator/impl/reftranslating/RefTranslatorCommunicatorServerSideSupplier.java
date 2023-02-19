package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;

@FunctionalInterface
public interface RefTranslatorCommunicatorServerSideSupplier extends RefTranslatorCommunicatorSupplier
{
	@Override
	public <REF_TO> StudentSideCommunicatorServerSide<REF_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks);
}
