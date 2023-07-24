package net.haspamelodica.charon.marshaling;

import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;

public interface MarshalerCallbacks<REF, TYPEREF extends REF, SST, SSX extends StudentSideCausedException>
{
	public Object createForwardRef(UntranslatedRef<REF, TYPEREF> untranslatedRef);
	public String getCallbackInterfaceCn(Object translatedRef);
	
	public String typerefToString(TYPEREF typeref);

	public SST checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject);
	public SSX newStudentCausedException(SST studentSideThrowable);
}
