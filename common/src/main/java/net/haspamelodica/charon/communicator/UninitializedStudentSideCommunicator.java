package net.haspamelodica.charon.communicator;

public interface UninitializedStudentSideCommunicator<REF,
		THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		TC extends Transceiver, CM extends CallbackManager>
{
	public StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
			? extends TC, ? extends CM> initialize(StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF> callbacks);
}
