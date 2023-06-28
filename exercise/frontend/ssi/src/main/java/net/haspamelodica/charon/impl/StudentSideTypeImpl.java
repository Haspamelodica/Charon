package net.haspamelodica.charon.impl;

import java.util.List;
import java.util.Optional;

import net.haspamelodica.charon.StudentSideType;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.util.LazyValue;

public class StudentSideTypeImpl<REF, TYPEREF extends REF> implements StudentSideType
{
	private final MarshalingCommunicator<REF, TYPEREF, ?, ?, ?, ?>	communicator;
	private final TYPEREF											typeref;
	private final LazyValue<StudentSideTypeDescription<TYPEREF>>	description;

	public StudentSideTypeImpl(MarshalingCommunicator<REF, TYPEREF, ?, ?, ?, ?> communicator, TYPEREF typeref)
	{
		this.communicator = communicator;
		this.typeref = typeref;
		this.description = new LazyValue<>(() -> communicator.describeType(typeref));
	}

	@Override
	public Kind kind()
	{
		return switch(describe().kind())
		{
			case PRIMITIVE -> StudentSideType.Kind.PRIMITIVE;
			case ARRAY -> StudentSideType.Kind.ARRAY;
			case CLASS -> StudentSideType.Kind.CLASS;
			case INTERFACE -> StudentSideType.Kind.INTERFACE;
		};
	}

	@Override
	public String name()
	{
		return describe().name();
	}

	@Override
	public Optional<StudentSideType> superclass()
	{
		return wrapStudentSideType(describe().superclass());
	}

	@Override
	public List<StudentSideType> superinterfaces()
	{
		return wrapStudentSideType(describe().superinterfaces());
	}

	@Override
	public Optional<StudentSideType> componentTypeIfArray()
	{
		return wrapStudentSideType(describe().componentTypeIfArray());
	}

	public TYPEREF getTyperef()
	{
		return typeref;
	}

	private List<StudentSideType> wrapStudentSideType(List<TYPEREF> typeInformations)
	{
		return typeInformations.stream().map(this::wrapStudentSideType).toList();
	}
	private Optional<StudentSideType> wrapStudentSideType(Optional<TYPEREF> typeInformation)
	{
		return typeInformation.map(this::wrapStudentSideType);
	}
	private StudentSideType wrapStudentSideType(TYPEREF type)
	{
		return new StudentSideTypeImpl<>(communicator, type);
	}

	private StudentSideTypeDescription<TYPEREF> describe()
	{
		return description.get();
	}
}
