package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public class RefTranslatorInternalCallbackManagerImpl<REF_TO, REF_FROM>
		extends RefTranslatorCallbackManagerImpl<REF_TO, REF_FROM>
		implements InternalCallbackManager<REF_TO>
{
	public RefTranslatorInternalCallbackManagerImpl(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator)
	{
		super(communicator, translator);
	}

	@Override
	public REF_TO createCallbackInstance(String interfaceCn)
	{
		return translator.translateTo(communicator.getCallbackManager().createCallbackInstance(interfaceCn));
	}

	public static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM, TC_FROM extends Transceiver>
			BiFunction<StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>,
					RefTranslator<REF_TO, REF_FROM>, InternalCallbackManager<REF_TO>>
			supplier()
	{
		return RefTranslatorInternalCallbackManagerImpl::create;
	}

	public static <REF_TO, REF_FROM>
			InternalCallbackManager<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator)
	{
		return new RefTranslatorInternalCallbackManagerImpl<>(communicator, translator);
	}
}