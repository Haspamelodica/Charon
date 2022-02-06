package net.haspamelodica.studentcodeseparator.exceptions;

public class CommunicationException extends StudentCodeSeparatorException
{
	public CommunicationException()
	{}
	public CommunicationException(String message)
	{
		super(message);
	}
	public CommunicationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public CommunicationException(Throwable cause)
	{
		super(cause);
	}
	protected CommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
