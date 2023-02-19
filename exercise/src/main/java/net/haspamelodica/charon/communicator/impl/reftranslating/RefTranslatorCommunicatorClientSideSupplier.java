package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;

@FunctionalInterface
public interface RefTranslatorCommunicatorClientSideSupplier extends RefTranslatorCommunicatorSupplier
{
	@Override
	public <REF_TO> StudentSideCommunicatorClientSide<REF_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks);
}
