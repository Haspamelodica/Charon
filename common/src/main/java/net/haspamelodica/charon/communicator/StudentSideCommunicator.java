package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.OperationOutcome;

public interface StudentSideCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
{
	public boolean storeRefsIdentityBased();

	public OperationOutcome<REF, TYPEREF> getTypeByName(String typeName);
	public TYPEREF getArrayType(TYPEREF componentType);
	public TYPEREF getTypeOf(REF ref);
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type);

	public TYPEREF getTypeHandledBySerdes(REF serdesRef);

	//TODO rename to create/initializeArray to stay consistent
	public OperationOutcome<REF, TYPEREF> newArray(TYPEREF arrayType, int length);
	public OperationOutcome<REF, TYPEREF> newMultiArray(TYPEREF arrayType, List<Integer> dimensions);
	public OperationOutcome<REF, TYPEREF> newArrayWithInitialValues(TYPEREF arrayType, List<REF> initialValues);
	public int getArrayLength(REF arrayRef);
	public OperationOutcome<REF, TYPEREF> getArrayElement(REF arrayRef, int index);
	public OperationOutcome<Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef);

	public OperationOutcome<REF, TYPEREF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs);

	public OperationOutcome<REF, TYPEREF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs);
	public OperationOutcome<REF, TYPEREF> getStaticField(TYPEREF type, String name, TYPEREF fieldType);
	public OperationOutcome<Void, TYPEREF> setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef);

	public OperationOutcome<REF, TYPEREF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs);
	public OperationOutcome<REF, TYPEREF> getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef);
	public OperationOutcome<Void, TYPEREF> setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef);

	public TC getTransceiver();
	public CM getCallbackManager();
}
