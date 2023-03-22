package net.haspamelodica.charon.communicator;

public interface UninitializedStudentSideCommunicator<REF, TC extends Transceiver, CM extends CallbackManager>
{
	public StudentSideCommunicator<REF, ? extends TC, ? extends CM> initialize(StudentSideCommunicatorCallbacks<REF> callbacks);
}
