package net.haspamelodica.charon.marshaling;

import net.haspamelodica.charon.exceptions.StudentSideCausedException;

public interface MarshalerCallbacks<SST, SSX extends StudentSideCausedException>
{
	public SST checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject);
	public SSX newStudentCausedException(SST studentSideThrowable, String studentSideThrowableClassname);
}
