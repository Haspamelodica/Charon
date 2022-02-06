package net.haspamelodica.studentcodeseparator;

/**
 * Test code has access to one instance of {@link StudentSide}. TODO how?
 * <p>
 * This interface is not intended to be subclassed by test code.
 */
public interface StudentSide
{
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass);
}
