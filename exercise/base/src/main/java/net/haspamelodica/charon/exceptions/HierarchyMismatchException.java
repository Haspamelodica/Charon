package net.haspamelodica.charon.exceptions;

public class HierarchyMismatchException extends StudentSideCausedException
{
	public HierarchyMismatchException()
	{}
	public HierarchyMismatchException(String message)
	{
		super(message);
	}
	public HierarchyMismatchException(String message, Throwable cause)
	{
		super(message, cause);
	}
	public HierarchyMismatchException(Throwable cause)
	{
		super(cause);
	}
	protected HierarchyMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public HierarchyMismatchException withContext(String message)
	{
		return new HierarchyMismatchException(message, this);
	}
}
