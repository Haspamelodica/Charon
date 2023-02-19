package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class RefTranslatorCommunicatorSupplierImpl<REF_FROM, COMM extends StudentSideCommunicator<REF_FROM>>
		implements RefTranslatorCommunicatorSupplier
{
	protected final COMM communicator;

	public RefTranslatorCommunicatorSupplierImpl(COMM communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public <REF_TO> StudentSideCommunicator<REF_TO> createCommunicator(boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorCommunicator<>(communicator, storeRefsIdentityBased, callbacks);
	}
}
