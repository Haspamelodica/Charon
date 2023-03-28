package net.haspamelodica.charon.exceptions;

import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

// TODO let this class have a SSI of the student-side exception
public class StudentSideException extends StudentSideCausedException
{
	private final ThrowableSSI studentSideCause;

	public StudentSideException(ThrowableSSI studentSideCause, String studentSideCauseClassname)
	{
		super(studentSideCauseClassname + ": " + studentSideCause.getMessage());
		this.studentSideCause = studentSideCause;
	}

	public ThrowableSSI getStudentSideCause()
	{
		return studentSideCause;
	}
}
