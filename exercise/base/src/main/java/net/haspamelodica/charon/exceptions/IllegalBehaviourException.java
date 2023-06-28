package net.haspamelodica.charon.exceptions;

public class IllegalBehaviourException extends StudentSideCausedException
{
	public IllegalBehaviourException()
	{}
	public IllegalBehaviourException(String message)
	{
		super(message);
	}
	public IllegalBehaviourException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public IllegalBehaviourException(Throwable cause)
	{
		super(cause);
	}
	protected IllegalBehaviourException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public IllegalBehaviourException withContext(String message)
	{
		return new IllegalBehaviourException(message, this);
	}
}
