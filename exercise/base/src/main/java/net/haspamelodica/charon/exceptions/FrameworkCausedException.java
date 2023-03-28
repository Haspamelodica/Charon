package net.haspamelodica.charon.exceptions;

public class FrameworkCausedException extends CharonException
{
	public FrameworkCausedException()
	{}
	public FrameworkCausedException(String message)
	{
		super(message);
	}
	public FrameworkCausedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public FrameworkCausedException(Throwable cause)
	{
		super(cause);
	}
	protected FrameworkCausedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
