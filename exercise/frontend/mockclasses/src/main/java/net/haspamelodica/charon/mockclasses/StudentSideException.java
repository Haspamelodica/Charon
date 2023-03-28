package net.haspamelodica.charon.mockclasses;

import net.haspamelodica.charon.exceptions.StudentSideCausedException;

public class StudentSideException extends StudentSideCausedException
{
	private final Throwable studentSideCause;

	public StudentSideException(Throwable studentSideCause)
	{
		this.studentSideCause = studentSideCause;
	}

	public Throwable getStudentSideCause()
	{
		return studentSideCause;
	}
}
