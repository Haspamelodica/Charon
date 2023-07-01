package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public class RefTranslatorInternalCallbackManagerImpl<REF_TO, REF_FROM>
		extends RefTranslatorCallbackManagerImpl<REF_TO, REF_FROM>
		implements InternalCallbackManager<REF_TO>
{
	private final RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO> callbacks;

	public RefTranslatorInternalCallbackManagerImpl(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?,
			? extends InternalCallbackManager<REF_FROM>> communicator, RefTranslator<REF_TO, REF_FROM> translator,
			RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO> callbacks)
	{
		super(communicator, translator);
		this.callbacks = callbacks;
	}

	@Override
	public REF_TO createCallbackInstance(String interfaceCn)
	{
		REF_FROM refFrom = communicator.getCallbackManager().createCallbackInstance(interfaceCn);
		REF_TO refTo = callbacks.createBackwardRef(new UntranslatedRef<>(communicator, refFrom));
		translator.setBackwardRefTranslation(refFrom, refTo);
		return refTo;
	}

	public static <REF_TO, REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
			CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
			TC_FROM extends Transceiver>
			RefTranslatorCommunicatorPartSupplier<REF_TO,
					REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM, CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM,
					TC_FROM, RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>, InternalCallbackManager<REF_TO>>
			supplier()
	{
		return RefTranslatorInternalCallbackManagerImpl::create;
	}

	public static <REF_TO, REF_FROM> InternalCallbackManager<REF_TO>
			create(StudentSideCommunicator<REF_FROM, ?, ?, ?, ?, ?, ?, ? extends InternalCallbackManager<REF_FROM>> communicator,
					RefTranslator<REF_TO, REF_FROM> translator,
					RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO> callbacks)
	{
		return new RefTranslatorInternalCallbackManagerImpl<>(communicator, translator, callbacks);
	}
}
