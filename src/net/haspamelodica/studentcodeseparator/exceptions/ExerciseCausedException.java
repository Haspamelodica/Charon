package net.haspamelodica.studentcodeseparator.exceptions;

public class ExerciseCausedException extends StudentCodeSeparatorException
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
