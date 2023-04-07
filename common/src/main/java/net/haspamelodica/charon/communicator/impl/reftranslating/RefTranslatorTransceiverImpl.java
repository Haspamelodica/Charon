package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public abstract class RefTranslatorTransceiverImpl<
		REF_TO,
		REF_FROM,
		TC_FROM extends Transceiver>
		implements Transceiver
{
	protected final StudentSideCommunicator<REF_FROM, ?, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>	communicator;
	protected final RefTranslator<REF_TO, REF_FROM>																			translator;

	protected RefTranslatorTransceiverImpl(StudentSideCommunicator<REF_FROM, ?, ? extends TC_FROM,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		this.communicator = communicator;
		this.translator = translator;
	}
}