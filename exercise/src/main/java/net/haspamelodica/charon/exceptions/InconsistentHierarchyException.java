package net.haspamelodica.charon.exceptions;

public class InconsistentHierarchyException extends ExerciseCausedException
{
	public InconsistentHierarchyException()
	{}
	public InconsistentHierarchyException(String message)
	{
		super(message);
	}
	public InconsistentHierarchyException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public InconsistentHierarchyException(Throwable cause)
	{
		super(cause);
	}
	protected InconsistentHierarchyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
