package net.haspamelodica.charon;

import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.exceptions.MissingSerDesException;

/** This interface is not intended to be subclassed by test code. */
public interface StudentSide
{
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
			throws InconsistentHierarchyException, MissingSerDesException;

	public String getStudentSideClassname(StudentSideInstance ssi);
	public String getStudentSideClassname(StudentSidePrototype<?> prototype);
	public boolean isInstance(StudentSidePrototype<?> prototype, StudentSideInstance ssi);
	public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SI cast(SP prototype, StudentSideInstance ssi);
}
