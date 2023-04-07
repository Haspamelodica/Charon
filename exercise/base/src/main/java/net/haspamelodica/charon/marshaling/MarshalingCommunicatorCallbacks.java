package net.haspamelodica.charon.marshaling;

import java.util.List;

import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedTyperef;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;

public interface MarshalingCommunicatorCallbacks<REF, TYPEREF extends REF, M, SST, SSX extends StudentSideCausedException>
{
	public Class<?> lookupLocalType(UntranslatedTyperef<REF, TYPEREF> untranslatedTyperef);

	/** Creates and returns a representation object for the given {@link UntranslatedRef}. */
	public Object createRepresentationObject(StudentSideType<TYPEREF, ?> type, UntranslatedRef<REF, TYPEREF> untranslatedRef);
	public String getCallbackInterfaceCn(Object exerciseSideObject);

	public CallbackMethod<M> lookupCallbackInstanceMethod(StudentSideType<TYPEREF, ?> receiverStaticType, Class<?> receiverDynamicType,
			String name, StudentSideType<TYPEREF, ?> returnType, List<StudentSideType<TYPEREF, ?>> params);
	/** This method will only be called for methods where lookupCallbackInstanceMethod has been called before. */
	public Object callCallbackInstanceMethodChecked(M methodData, Object receiver, List<Object> args)
			throws ExceptionInTargetException;

	public static record CallbackMethod<M>(List<Class<? extends SerDes<?>>> additionalSerdeses, M methodData)
	{}

	public SST checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject);
	public SSX newStudentCausedException(SST studentSideThrowable);
}
