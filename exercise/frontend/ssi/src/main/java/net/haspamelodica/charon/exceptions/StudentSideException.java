package net.haspamelodica.charon.exceptions;

import net.haspamelodica.charon.StudentSideType;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

public class StudentSideException extends StudentSideCausedException
{
	private final ThrowableSSI studentSideCause;

	public StudentSideException(ThrowableSSI studentSideCause, StudentSideType studentSideCauseType)
	{
		super(buildMessage(studentSideCause, studentSideCauseType));
		this.studentSideCause = studentSideCause;
	}

	private static String buildMessage(ThrowableSSI studentSideCause, StudentSideType studentSideCauseType)
	{
		String studentSideMessage = studentSideCause.getMessage();
		return studentSideCauseType.name() + (studentSideMessage != null ? ": " + studentSideMessage : "");
	}

	public ThrowableSSI getStudentSideCause()
	{
		return studentSideCause;
	}
}
