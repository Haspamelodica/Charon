package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.OperationOutcome;

public interface StudentSideCommunicator<REF,
		THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		TC extends Transceiver, CM extends CallbackManager>
{
	public boolean storeRefsIdentityBased();

	public OperationOutcome<TYPEREF, Void, TYPEREF> getTypeByName(String typeName);
	public TYPEREF getArrayType(TYPEREF componentType);
	public TYPEREF getTypeOf(REF ref);
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type);

	public TYPEREF getTypeHandledBySerdes(REF serdesRef);

	public OperationOutcome<REF, Void, TYPEREF> createArray(TYPEREF arrayType, int length);
	public OperationOutcome<REF, Void, TYPEREF> createMultiArray(TYPEREF arrayType, List<Integer> dimensions);
	public OperationOutcome<REF, Void, TYPEREF> initializeArray(TYPEREF arrayType, List<REF> initialValues);
	public int getArrayLength(REF arrayRef);
	public OperationOutcome<REF, Void, TYPEREF> getArrayElement(REF arrayRef, int index);
	public OperationOutcome<Void, Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef);

	public OperationOutcome<CONSTRUCTORREF, Void, TYPEREF> lookupConstructor(TYPEREF type, List<TYPEREF> params);
	public OperationOutcome<METHODREF, Void, TYPEREF> lookupMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, boolean isStatic);
	public OperationOutcome<FIELDREF, Void, TYPEREF> lookupField(TYPEREF type, String name, TYPEREF fieldType, boolean isStatic);

	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callConstructor(CONSTRUCTORREF constructor, List<REF> argRefs);

	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callStaticMethod(METHODREF method, List<REF> argRefs);
	public OperationOutcome<REF, Void, TYPEREF> getStaticField(FIELDREF field);
	public OperationOutcome<Void, Void, TYPEREF> setStaticField(FIELDREF field, REF valueRef);

	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callInstanceMethod(METHODREF method, REF receiverRef, List<REF> argRefs);
	public OperationOutcome<REF, Void, TYPEREF> getInstanceField(FIELDREF field, REF receiverRef);
	public OperationOutcome<Void, Void, TYPEREF> setInstanceField(FIELDREF field, REF receiverRef, REF valueRef);

	public TC getTransceiver();
	public CM getCallbackManager();
}
