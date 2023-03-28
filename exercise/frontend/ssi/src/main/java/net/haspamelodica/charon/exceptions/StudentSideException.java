package net.haspamelodica.charon.exceptions;

import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

// TODO let this class have a SSI of the student-side exception
public class StudentSideException extends StudentSideCausedException
{
	private final ThrowableSSI studentSideCause;

	public StudentSideException(ThrowableSSI studentSideCause, String studentSideCauseClassname)
	{
		super(buildMessage(studentSideCause, studentSideCauseClassname));
		this.studentSideCause = studentSideCause;
	}

	private static String buildMessage(ThrowableSSI studentSideCause, String studentSideCauseClassname)
	{
		String studentSideMessage = studentSideCause.getMessage();
		return studentSideCauseClassname + (studentSideMessage != null ? ": " + studentSideMessage : "");
	}

	public ThrowableSSI getStudentSideCause()
	{
		return studentSideCause;
	}
}
