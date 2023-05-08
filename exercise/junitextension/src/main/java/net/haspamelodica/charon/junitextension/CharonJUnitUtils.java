package net.haspamelodica.charon.junitextension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

public class CharonJUnitUtils
{
	/**
	 * Like {@link #assertStudentThrows(StudentSide, Class, Runnable)},
	 * but with a Class argument instead of a {@link StudentSidePrototype}.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			Class<? extends StudentSidePrototype<T>> expectedType, Runnable runnable)
	{
		return assertStudentThrows(studentSide, studentSide.createPrototype(expectedType), runnable);
	}

	/**
	 * Mimics {@link Assertions#assertThrows(Class, Executable)} for student-side exceptions.
	 * <p>
	 * <em>Assert</em> that execution of the supplied {@code runnable} throws
	 * a student-side exception of the {@code expectedType} and return the exception.
	 * <p>
	 * If no exception is thrown, or if a student-side exception of a different type is thrown, this method will fail.
	 * Eexception which are not a student-side exceptions will not be caught by this method.
	 * <p>
	 * If you do not want to perform additional checks on the exception student side instance,
	 * ignore the return value.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			StudentSidePrototype<T> expectedType, Runnable runnable)
	{
		return assertStudentThrows(studentSide, expectedType, runnable, () -> "Unexpected student-side exception type thrown");
	}

	/**
	 * Like {@link #assertStudentThrows(StudentSide, Class, Runnable,String)},
	 * but with a Class argument instead of a {@link StudentSidePrototype}.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			Class<? extends StudentSidePrototype<T>> expectedType, Runnable runnable, String message)
	{
		return assertStudentThrows(studentSide, studentSide.createPrototype(expectedType), runnable, message);
	}

	/**
	 * Mimics {@link Assertions#assertThrows(Class, Executable, String)} for student-side exceptions.
	 * <p>
	 * <em>Assert</em> that execution of the supplied {@code runnable} throws
	 * a student-side exception of the {@code expectedType} and return the exception.
	 * <p>
	 * If no exception is thrown, or if a student-side exception of a different type is thrown, this method will fail.
	 * Eexception which are not a student-side exceptions will not be caught by this method.
	 * <p>
	 * If you do not want to perform additional checks on the exception student side instance,
	 * ignore the return value.
	 * <p>
	 * Fails with the supplied failure {@code message}.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			StudentSidePrototype<T> expectedType, Runnable runnable, String message)
	{
		return assertStudentThrows(studentSide, expectedType, runnable, () -> message);
	}

	/**
	 * Like {@link #assertStudentThrows(StudentSide, Class, Runnable, Supplier)},
	 * but with a Class argument instead of a {@link StudentSidePrototype}.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			Class<? extends StudentSidePrototype<T>> expectedType, Runnable runnable, Supplier<String> messageSupplier)
	{
		return assertStudentThrows(studentSide, studentSide.createPrototype(expectedType), runnable, messageSupplier);
	}

	/**
	 * Mimics {@link Assertions#assertThrows(Class, Executable, Supplier)} for student-side exceptions.
	 * <p>
	 * <em>Assert</em> that execution of the supplied {@code runnable} throws
	 * a student-side exception of the {@code expectedType} and return the exception.
	 * <p>
	 * If no exception is thrown, or if a student-side exception of a different type is thrown, this method will fail.
	 * Eexception which are not a student-side exceptions will not be caught by this method.
	 * <p>
	 * If you do not want to perform additional checks on the exception student side instance,
	 * ignore the return value.
	 * <p>
	 * If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 */
	public static <T extends ThrowableSSI> T assertStudentThrows(StudentSide studentSide,
			StudentSidePrototype<T> expectedType, Runnable runnable, Supplier<String> messageSupplier)
	{
		try
		{
			runnable.run();
		} catch(StudentSideException sse)
		{
			ThrowableSSI e = sse.getStudentSideCause();
			if(studentSide.isInstance(expectedType, e))
				// return to skip the fail call after try-catch block
				return studentSide.cast(expectedType, e);

			// This call to assertEquals() should always fail. assertEquals() is used instead of fail() to have proper error message formatting.
			assertEquals(studentSide.getStudentSideType(expectedType).name(), studentSide.getStudentSideType(e).name(), messageSupplier);
			return fail("Internal error: assertEquals succeeded unexpectedly");
		}
		return fail("Expected a student-side exception of type " + studentSide.getStudentSideType(expectedType).name() + ", but nothing was thrown");
	}
}
