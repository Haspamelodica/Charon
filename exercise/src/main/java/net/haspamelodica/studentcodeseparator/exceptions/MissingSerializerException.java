package net.haspamelodica.studentcodeseparator.exceptions;

public class MissingSerializerException extends ExerciseCausedException
{
	public MissingSerializerException()
	{}
	public MissingSerializerException(String message)
	{
		super(message);
	}
	public MissingSerializerException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public MissingSerializerException(Throwable cause)
	{
		super(cause);
	}
	protected MissingSerializerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
