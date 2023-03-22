package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.ExternalCallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public class RefTranslatorExternalCallbackManagerImpl<REF_TO, REF_FROM>
		extends RefTranslatorCallbackManagerImpl<REF_TO, REF_FROM>
		implements ExternalCallbackManager<REF_TO>
{
	public RefTranslatorExternalCallbackManagerImpl(StudentSideCommunicator<REF_FROM, ?,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		super(communicator, translator);
	}

	@Override
	public void createCallbackInstance(REF_TO callbackRef, String interfaceCn)
	{
		translator.setForwardRefTranslation(communicator.getCallbackManager().createCallbackInstance(interfaceCn), callbackRef);
	}

	public static <REF_TO, REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, ExternalCallbackManager<REF_TO>>
			supplier()
	{
		return RefTranslatorExternalCallbackManagerImpl::create;
	}

	public static <REF_TO, REF_FROM>
			ExternalCallbackManager<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator)
	{
		return new RefTranslatorExternalCallbackManagerImpl<>(communicator, translator);
	}
}