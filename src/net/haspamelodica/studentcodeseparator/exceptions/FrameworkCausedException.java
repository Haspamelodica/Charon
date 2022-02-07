package net.haspamelodica.studentcodeseparator.exceptions;

public class FrameworkCausedException extends StudentCodeSeparatorException
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
