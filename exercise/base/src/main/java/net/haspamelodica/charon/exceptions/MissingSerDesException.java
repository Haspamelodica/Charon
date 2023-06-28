package net.haspamelodica.charon.exceptions;

public class MissingSerDesException extends ExerciseCausedException
{
	public MissingSerDesException()
	{}
	public MissingSerDesException(String message)
	{
		super(message);
	}
	public MissingSerDesException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public MissingSerDesException(Throwable cause)
	{
		super(cause);
	}
	protected MissingSerDesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public MissingSerDesException withContext(String message)
	{
		return new MissingSerDesException(message, this);
	}
}
