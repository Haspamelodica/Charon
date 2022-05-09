package net.haspamelodica.charon.utils.communication;

public class IncorrectUsageException extends Exception
{
	public IncorrectUsageException()
	{
		super();
	}
	public IncorrectUsageException(String message)
	{
		super(message);
	}
	public IncorrectUsageException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public IncorrectUsageException(Throwable cause)
	{
		super(cause);
	}
	protected IncorrectUsageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
