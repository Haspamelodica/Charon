package net.haspamelodica.charon.communicator;

import java.util.List;

public interface StudentSideCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
{
	public boolean storeRefsIdentityBased();

	public TYPEREF getTypeByName(String typeName);
	public TYPEREF getArrayType(TYPEREF componentType);
	public TYPEREF getTypeOf(REF ref);
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type);

	public REF newArray(TYPEREF componentType, int length);
	public REF newMultiArray(TYPEREF componentType, List<Integer> dimensions);
	//TODO get / set elements

	public RefOrError<REF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs);

	public RefOrError<REF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs);
	public REF getStaticField(TYPEREF type, String name, TYPEREF fieldType);
	public void setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef);

	public RefOrError<REF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs);
	public REF getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef);
	public void setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef);

	public TC getTransceiver();
	public CM getCallbackManager();
}
