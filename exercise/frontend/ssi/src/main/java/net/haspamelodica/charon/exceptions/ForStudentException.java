package net.haspamelodica.charon.exceptions;

import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

public class ForStudentException extends StudentSideCausedException
{
	private final ThrowableSSI studentSideCause;

	public ForStudentException(ThrowableSSI studentSideCause)
	{
		super(studentSideCause.getMessage());
		this.studentSideCause = studentSideCause;
	}

	public ThrowableSSI getStudentSideCause()
	{
		return studentSideCause;
	}

	// don't override withContext; we can't do better than a StudentSideCausedException
}
