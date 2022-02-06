package net.haspamelodica.studentcodeseparator.exceptions;

public class SerializationException extends StudentCodeSeparatorException
{
	public SerializationException()
	{}
	public SerializationException(String message)
	{
		super(message);
	}
	public SerializationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public SerializationException(Throwable cause)
	{
		super(cause);
	}
	protected SerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
