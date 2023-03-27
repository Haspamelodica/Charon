package net.haspamelodica.charon.communicator;

public record RefOrError<REF>(REF resultOrErrorRef, boolean isError)
{
	public boolean isSuccess()
	{
		return !isError();
	}

	@Override
	public String toString()
	{
		return isError() ? "error " + resultOrErrorRef() : String.valueOf(resultOrErrorRef());
	}

	public static <REF> RefOrError<REF> success(REF ref)
	{
		return new RefOrError<REF>(ref, false);
	}
	public static <REF> RefOrError<REF> error(REF ref)
	{
		return new RefOrError<REF>(ref, true);
	}
}
