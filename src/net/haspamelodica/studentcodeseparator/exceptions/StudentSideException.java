package net.haspamelodica.studentcodeseparator.exceptions;

public class StudentSideException extends RuntimeException
{
	public StudentSideException()
	{}
	public StudentSideException(String message)
	{
		super(message);
	}
	public StudentSideException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public StudentSideException(Throwable cause)
	{
		super(cause);
	}
	protected StudentSideException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
