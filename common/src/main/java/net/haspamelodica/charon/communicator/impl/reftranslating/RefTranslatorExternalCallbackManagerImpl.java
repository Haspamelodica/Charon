package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.ExternalCallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public class RefTranslatorExternalCallbackManagerImpl<REF_TO, REF_FROM>
		extends RefTranslatorCallbackManagerImpl<REF_TO, REF_FROM>
		implements ExternalCallbackManager<REF_TO>
{
	public RefTranslatorExternalCallbackManagerImpl(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		super(communicator, translator);
	}

	@Override
	public void createCallbackInstance(REF_TO callbackRef, String interfaceCn)
	{
		translator.setBackwardRefTranslation(communicator.getCallbackManager().createCallbackInstance(interfaceCn), callbackRef);
	}

	public static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					TC_FROM, RefTranslatorCommunicatorCallbacks<REF_TO>, ExternalCallbackManager<REF_TO>>
			supplier()
	{
		return RefTranslatorExternalCallbackManagerImpl::create;
	}

	public static <REF_TO, REF_FROM>
			ExternalCallbackManager<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator,
					RefTranslatorCommunicatorCallbacks<?> callbacks)
	{
		return new RefTranslatorExternalCallbackManagerImpl<>(communicator, translator);
	}
}
