package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public abstract class RefTranslatorCallbackManagerImpl<REF_TO, REF_FROM, TYPEREF_FROM extends REF_FROM> implements CallbackManager
{
	protected final StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ?, ? extends InternalCallbackManager<REF_FROM>>	communicator;
	protected final RefTranslator<REF_TO, REF_FROM>																	translator;

	protected RefTranslatorCallbackManagerImpl(StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ?,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		this.communicator = communicator;
		this.translator = translator;
	}
}
