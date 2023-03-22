package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class UntranslatedRef<REF> implements UntypedUntranslatedRef
{
	private final StudentSideCommunicator<REF, ?, ?>	communicator;
	private final REF									ref;

	public UntranslatedRef(StudentSideCommunicator<REF, ?, ?> communicator, REF ref)
	{
		this.communicator = communicator;
		this.ref = ref;
	}

	@Override
	public String getClassname()
	{
		return communicator.getClassname(ref);
	}

	public REF ref()
	{
		return ref;
	}
	public StudentSideCommunicator<REF, ?, ?> communicator()
	{
		return communicator;
	}
}