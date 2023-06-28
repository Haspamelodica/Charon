package net.haspamelodica.charon.exceptions;

public class StudentSideCausedException extends CharonException
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

	@Override
	public StudentSideCausedException withContext(String message)
	{
		return new StudentSideCausedException(message, this);
	}
}
