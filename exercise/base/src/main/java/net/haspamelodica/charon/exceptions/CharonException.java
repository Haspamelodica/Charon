package net.haspamelodica.charon.exceptions;

public class CharonException extends RuntimeException
{
	public CharonException()
	{}
	public CharonException(String message)
	{
		super(message);
	}
	public CharonException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public CharonException(Throwable cause)
	{
		super(cause);
	}
	protected CharonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CharonException withContext(String message)
	{
		return new CharonException(message, this);
	}
}
