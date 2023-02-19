package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;

public class RefTranslatorCommunicatorClientSideSupplierImpl<REF_FROM>
		extends RefTranslatorCommunicatorSupplierImpl<REF_FROM, StudentSideCommunicatorClientSide<REF_FROM>>
		implements RefTranslatorCommunicatorClientSideSupplier
{
	public RefTranslatorCommunicatorClientSideSupplierImpl(StudentSideCommunicatorClientSide<REF_FROM> communicator)
	{
		super(communicator);
	}

	@Override
	public <REF_TO> StudentSideCommunicatorClientSide<REF_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorCommunicatorClientSide<>(communicator, storeRefsIdentityBased, callbacks);
	}
}
