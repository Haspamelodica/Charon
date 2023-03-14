package net.haspamelodica.charon.communicator;

public interface UninitializedStudentSideCommunicator<REF, COMM extends StudentSideCommunicator<REF>>
{
	public COMM initialize(StudentSideCommunicatorCallbacks<REF> callbacks);
}
