package net.haspamelodica.studentcodeseparator.exceptions;

public class StudentSideCausedException extends StudentCodeSeparatorException
{
	public StudentSideCausedException()
	{}
	public StudentSideCausedException(String message)
	{
		super(message);
	}
	public StudentSideCausedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public StudentSideCausedException(Throwable cause)
	{
		super(cause);
	}
	protected StudentSideCausedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
