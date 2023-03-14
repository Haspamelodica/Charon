package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class RefTranslatorCommunicatorSupplierImpl<REF_FROM, COMM extends StudentSideCommunicator<REF_FROM>>
		implements RefTranslatorCommunicatorSupplier
{
	protected final UninitializedStudentSideCommunicator<REF_FROM, COMM> communicator;

	public RefTranslatorCommunicatorSupplierImpl(UninitializedStudentSideCommunicator<REF_FROM, COMM> communicator)
	{
		this.communicator = communicator;
	}

	@Override
	public <REF_TO> StudentSideCommunicator<REF_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		return new RefTranslatorCommunicator<>(communicator, storeRefsIdentityBased, callbacks);
	}
}
