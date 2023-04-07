package net.haspamelodica.charon.communicator;

public interface UninitializedStudentSideCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
{
	public StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> initialize(StudentSideCommunicatorCallbacks<REF, TYPEREF> callbacks);
}
