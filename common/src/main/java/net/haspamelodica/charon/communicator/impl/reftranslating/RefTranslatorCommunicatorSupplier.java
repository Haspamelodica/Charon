package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

@FunctionalInterface
public interface RefTranslatorCommunicatorSupplier<
		REF_TO,
		TC_TO extends Transceiver,
		CM_TO extends CallbackManager,
		CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>>
{
	public StudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, ? extends TC_TO, ? extends CM_TO>
			createCommunicator(
					boolean storeRefsIdentityBased,
					StudentSideCommunicatorCallbacks<REF_TO, REF_TO, REF_TO> callbacks,
					CALLBACKS refTranslatorCommunicatorCallbacks);

	public default UninitializedStudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, TC_TO, CM_TO>
			withReftransParameters(
					boolean storeRefsIdentityBased,
					CALLBACKS refTranslatorCommunicatorCallbacks)
	{
		return callbacks -> createCommunicator(storeRefsIdentityBased, callbacks, refTranslatorCommunicatorCallbacks);
	}
}
