package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;

// This class needs the REF parameter because otherwise javac complanins about type bounds for StudentSideCommunicator
public class UntranslatedTyperef<REF, TYPEREF extends REF>
{
	private final StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?>	communicator;
	private final TYPEREF													typeref;

	public UntranslatedTyperef(StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?, ?, ?> communicator, TYPEREF typeref)
	{
		this.communicator = communicator;
		this.typeref = typeref;
	}

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
