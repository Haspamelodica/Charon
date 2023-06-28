package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

@FunctionalInterface
public interface RefTranslatorCommunicatorPartSupplier<REF_TO,
		REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
		CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
		TC_FROM extends Transceiver, CALLBACKS extends RefTranslatorCommunicatorCallbacks<REF_TO>, R>
{
	public R apply(
			StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>> communicator,
			RefTranslator<REF_TO, REF_FROM> refTranslator,
			CALLBACKS callbacks);
}
