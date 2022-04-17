package net.haspamelodica.studentcodeseparator.refs;

public class IllegalRefException extends Exception
{
	public IllegalRefException()
	{}
	public IllegalRefException(String message)
	{
		super(message);
	}
	public IllegalRefException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public IllegalRefException(Throwable cause)
	{
		super(cause);
	}
	protected IllegalRefException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
