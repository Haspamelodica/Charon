package net.haspamelodica.studentcodeseparator.exceptions;

public class StudentCodeSeparatorException extends RuntimeException
{
	public StudentCodeSeparatorException()
	{}
	public StudentCodeSeparatorException(String message)
	{
		super(message);
	}
	public StudentCodeSeparatorException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public StudentCodeSeparatorException(Throwable cause)
	{
		super(cause);
	}
	protected StudentCodeSeparatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
