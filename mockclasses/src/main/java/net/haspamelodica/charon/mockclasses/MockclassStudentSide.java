package net.haspamelodica.charon.mockclasses;

public interface MockclassStudentSide
{
	/**
	 * Runs the specified action with the given parameters with mockclasses enabled.
	 * The specified action class has to have a public constructor with no parameters.
	 * The parameter and return classes have to be forced to be delegated.
	 */
	public <P, R, X extends Exception> R runWithMockclasses(Class<? extends MockclassesFunction<P, R, X>> actionClass,
			P params) throws X;

	public static interface MockclassesFunction<P, R, X extends Exception>
	{
		public R apply(P params) throws X;
	}
}
