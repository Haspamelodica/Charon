package net.haspamelodica.charon.mockclasses;

import java.util.List;

public interface MockclassStudentSide
{
	/**
	 * Creates an instance of class {@code IMPL} using the constructor with the given parameters and arguments,
	 * with mockclasses enabled, and returns the created instance as an instance of {@code INTERFACE}.
	 * The interface class ({@code INTERFACE}) and all parameter classes have to be forced to be delegated.
	 */
	public <INTERFACE, IMPL extends INTERFACE, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					List<Class<?>> parameterClasses, Object... arguments) throws X;

	/**
	 * Creates an instance of class {@code IMPL} using the constructor with no parameters,
	 * with mockclasses enabled, and returns the created instance as an instance of {@code INTERFACE}.
	 * The interface class ({@code INTERFACE}) has to be forced to be delegated.
	 */
	public default <INTERFACE, IMPL extends INTERFACE, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of());
	}

	/**
	 * Version of {@link #createInstanceWithMockclasses(Class, Class, List, Object...)} with 1 argument
	 */
	public default <INTERFACE, IMPL extends INTERFACE, P1, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					Class<P1> p1, P1 a1) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of(p1), a1);
	}

	/**
	 * Version of {@link #createInstanceWithMockclasses(Class, Class, List, Object...)} with 2 arguments
	 */
	public default <INTERFACE, IMPL extends INTERFACE, P1, P2, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					Class<P1> p1, P1 a1, Class<P2> p2, P2 a2) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of(p1, p2), a1, a2);
	}

	/**
	 * Version of {@link #createInstanceWithMockclasses(Class, Class, List, Object...)} with 3 arguments
	 */
	public default <INTERFACE, IMPL extends INTERFACE, P1, P2, P3, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					Class<P1> p1, P1 a1, Class<P2> p2, P2 a2, Class<P3> p3, P3 a3) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of(p1, p2, p3), a1, a2, a3);
	}

	/**
	 * Version of {@link #createInstanceWithMockclasses(Class, Class, List, Object...)} with 4 arguments
	 */
	public default <INTERFACE, IMPL extends INTERFACE, P1, P2, P3, P4, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					Class<P1> p1, P1 a1, Class<P2> p2, P2 a2, Class<P3> p3, P3 a3, Class<P4> p4, P4 a4) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of(p1, p2, p3, p4), a1, a2, a3, a4);
	}

	/**
	 * Version of {@link #createInstanceWithMockclasses(Class, Class, List, Object...)} with 5 arguments
	 */
	public default <INTERFACE, IMPL extends INTERFACE, P1, P2, P3, P4, P5, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					Class<P1> p1, P1 a1, Class<P2> p2, P2 a2, Class<P3> p3, P3 a3, Class<P4> p4, P4 a4, Class<P5> p5, P5 a5) throws X
	{
		return createInstanceWithMockclasses(interfaceClass, implementationClass,
				List.of(p1, p2, p3, p4, p5), a1, a2, a3, a4, a5);
	}
}
