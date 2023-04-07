package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideTypeDescription;

public interface UntypedUntranslatedTyperef
{
	public StudentSideTypeDescription<? extends UntypedUntranslatedTyperef> describe();
}