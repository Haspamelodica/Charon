package net.haspamelodica.charon;

import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.MissingSerializerException;

/**
 * Test code has access to one instance of {@link StudentSide}. TODO how?
 * <p>
 * This interface is not intended to be subclassed by test code.
 */
public interface StudentSide
{
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
			throws InconsistentHierarchyException, MissingSerializerException;
}
