package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class RefTranslatorCommunicatorServerSideSupplierImpl<REF_FROM>
		extends RefTranslatorCommunicatorSupplierImpl<REF_FROM, StudentSideCommunicatorServerSide<REF_FROM>>
		implements RefTranslatorCommunicatorServerSideSupplier
{
	public RefTranslatorCommunicatorServerSideSupplierImpl(UninitializedStudentSideCommunicator<REF_FROM, StudentSideCommunicatorServerSide<REF_FROM>> communicator)
	{
		super(communicator);
	}

	@Override
	public <REF_TO> StudentSideCommunicatorServerSide<REF_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorCommunicatorServerSide<>(communicator, storeRefsIdentityBased, callbacks);
	}
}
