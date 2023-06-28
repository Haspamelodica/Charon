package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;

// TODO This class maybe doesn't need the REF type parameter
public class UntranslatedTyperef<REF, TYPEREF extends REF> implements UntypedUntranslatedTyperef
{
	private final StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?>	communicator;
	private final TYPEREF													typeref;

	public UntranslatedTyperef(StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?> communicator, TYPEREF typeref)
	{
		this.communicator = communicator;
		this.typeref = typeref;
	}

	@Override
	public StudentSideTypeDescription<UntranslatedTyperef<REF, TYPEREF>> describe()
	{
		StudentSideTypeDescription<TYPEREF> result = communicator.describeType(typeref());
		return new StudentSideTypeDescription<>(
				result.kind(),
				result.name(),
				result.superclass().map(superclass -> new UntranslatedTyperef<>(communicator, superclass)),
				result.superinterfaces().stream().map(superinterface -> new UntranslatedTyperef<>(communicator, superinterface)).toList(),
				result.componentTypeIfArray().map(componentType -> new UntranslatedTyperef<>(communicator, componentType)));
	}

	public TYPEREF typeref()
	{
		return typeref;
	}
}
