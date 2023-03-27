package net.haspamelodica.charon.reflection;

public class ExceptionInTargetException extends Exception
{
	private final Throwable targetThrowable;

	public ExceptionInTargetException(Throwable targetThrowable)
	{
		super(targetThrowable);
		this.targetThrowable = targetThrowable;
	}

	public Throwable getTargetThrowable()
	{
		return targetThrowable;
	}
}
