package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class UntranslatedRef<REF, TYPEREF extends REF> implements UntypedUntranslatedRef
{
	private final StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?>	communicator;
	private final REF														ref;

	public UntranslatedRef(StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?> communicator, REF ref)
	{
		this.communicator = communicator;
		this.ref = ref;
	}

	public UntranslatedTyperef<REF, TYPEREF> getType()
	{
		return new UntranslatedTyperef<>(communicator, communicator.getTypeOf(ref));
	}

	public REF ref()
	{
		return ref;
	}
}