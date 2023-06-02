package net.haspamelodica.charon.exceptions;

public class CompilationErrorException extends StudentSideCausedException
{
	public CompilationErrorException()
	{}
	public CompilationErrorException(String message)
	{
		super(message);
	}
	public CompilationErrorException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public CompilationErrorException(Throwable cause)
	{
		super(cause);
	}
	protected CompilationErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
