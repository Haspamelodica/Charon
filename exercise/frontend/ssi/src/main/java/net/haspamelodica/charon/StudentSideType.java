package net.haspamelodica.charon;

import java.util.List;
import java.util.Optional;

// TODO this is basically StudentSideTypeDescription
public interface StudentSideType
{
	public Kind kind();
	public String name();

	public Optional<StudentSideType> superclass();
	public List<StudentSideType> superinterfaces();
	public Optional<StudentSideType> componentTypeIfArray();

	public static enum Kind
	{
		PRIMITIVE,
		ARRAY,
		CLASS,
		INTERFACE;
	}
}
