package net.haspamelodica.studentcodeseparator;

/**
 * Test code has access to one instance of {@link StudentSide}. TODO how?
 * <br>
 * This interface is not intended to be subclassed by test code.
 */
public interface StudentSide
{
	public <SO extends StudentSideObject, SP extends StudentSidePrototype<SO>> SP createPrototype(Class<SP> prototypeClass);
}
