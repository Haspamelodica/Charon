package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

@FunctionalInterface
public interface RefTranslatorCommunicatorSupplier<
		REF_TO,
		TC_TO extends Transceiver,
		CM_TO extends CallbackManager>
{
	public StudentSideCommunicator<REF_TO, ? extends TC_TO, ? extends CM_TO> createCommunicator(
			boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks);
}
