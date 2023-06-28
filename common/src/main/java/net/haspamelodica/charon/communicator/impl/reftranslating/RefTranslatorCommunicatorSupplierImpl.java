package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class RefTranslatorCommunicatorSupplierImpl<
		REF_TO,
		TC_TO extends Transceiver,
		CM_TO extends CallbackManager,
		REF_FROM, THROWABLEREF_FROM extends REF_FROM, TYPEREF_FROM extends REF_FROM,
		CONSTRUCTORREF_FROM extends REF_FROM, METHODREF_FROM extends REF_FROM, FIELDREF_FROM extends REF_FROM,
		TC_FROM extends Transceiver>
		implements RefTranslatorCommunicatorSupplier<REF_TO, TC_TO, CM_TO>
{
	private final UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
			CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator;

	private final BiFunction<StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
			CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, ? extends TC_FROM,
			? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver;

	private final BiFunction<StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
			CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, ? extends TC_FROM,
			? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager;

	public RefTranslatorCommunicatorSupplierImpl(
			UninitializedStudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
					CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
			BiFunction<StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
					CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, ? extends TC_FROM,
					? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver,
			BiFunction<StudentSideCommunicator<REF_FROM, THROWABLEREF_FROM, TYPEREF_FROM,
					CONSTRUCTORREF_FROM, METHODREF_FROM, FIELDREF_FROM, ? extends TC_FROM,
					? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		this.communicator = communicator;
		this.createTransceiver = createTransceiver;
		this.createCallbackManager = createCallbackManager;
	}


	@Override
	public StudentSideCommunicator<REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, REF_TO, ? extends TC_TO, ? extends CM_TO> createCommunicator(
			boolean storeRefsIdentityBased,
			StudentSideCommunicatorCallbacks<REF_TO, REF_TO, REF_TO> callbacks,
			RefTranslatorCommunicatorCallbacks<REF_TO> refTranslatorCommunicatorCallbacks)
	{
		return new RefTranslatorCommunicator<>(communicator, storeRefsIdentityBased, callbacks, refTranslatorCommunicatorCallbacks,
				createTransceiver, createCallbackManager);
	}
}
