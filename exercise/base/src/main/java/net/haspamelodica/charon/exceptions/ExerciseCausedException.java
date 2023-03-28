package net.haspamelodica.charon.exceptions;

public class ExerciseCausedException extends CharonException
{
	public ExerciseCausedException()
	{}
	public ExerciseCausedException(String message)
	{
		super(message);
	}
	public ExerciseCausedException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public ExerciseCausedException(Throwable cause)
	{
		super(cause);
	}
	protected ExerciseCausedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
