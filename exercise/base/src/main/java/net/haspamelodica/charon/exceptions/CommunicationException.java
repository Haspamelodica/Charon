package net.haspamelodica.charon.exceptions;

public class CommunicationException extends CharonException
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

	@Override
	public CommunicationException withContext(String message)
	{
		return new CommunicationException(message, this);
	}
}
