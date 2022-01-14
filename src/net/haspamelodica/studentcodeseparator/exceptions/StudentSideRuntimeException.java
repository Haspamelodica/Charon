package net.haspamelodica.studentcodeseparator.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

public class StudentSideRuntimeException extends StudentSideException
{
	private final String studentSideTrace;

	public StudentSideRuntimeException(String studentSideMessage, String studentSideTrace)
	{
		super(studentSideMessage);
		this.studentSideTrace = studentSideTrace;
	}

	@Override
	public void printStackTrace(PrintStream s)
	{
		super.printStackTrace(s);
		s.println("Student-side trace:");
		s.println(studentSideTrace);
	}
	@Override
	public void printStackTrace(PrintWriter s)
	{
		super.printStackTrace(s);
		s.println("Student-side trace:");
		s.println(studentSideTrace);
	}
}
