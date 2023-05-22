package net.haspamelodica.charon.communicator;

import java.util.List;

public interface StudentSideCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
{
	public boolean storeRefsIdentityBased();

	public TYPEREF getTypeByName(String typeName);
	public TYPEREF getArrayType(TYPEREF componentType);
	public TYPEREF getTypeOf(REF ref);
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type);

	public TYPEREF getTypeHandledBySerdes(REF serdesRef);

	public REF newArray(TYPEREF arrayType, int length);
	public REF newMultiArray(TYPEREF arrayType, List<Integer> dimensions);
	public REF newArrayWithInitialValues(TYPEREF arrayType, List<REF> initialValues);
	public int getArrayLength(REF arrayRef);
	public REF getArrayElement(REF arrayRef, int index);
	public void setArrayElement(REF arrayRef, int index, REF valueRef);

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
