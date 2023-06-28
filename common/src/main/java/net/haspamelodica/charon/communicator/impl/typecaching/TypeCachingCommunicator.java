package net.haspamelodica.charon.communicator.impl.typecaching;

import java.util.List;

import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public class TypeCachingCommunicator<REF,
		THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
{
	private final StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
			? extends TC, ? extends CM> communicator;

	private final UnidirectionalMap<String, OperationOutcome<TYPEREF, Void, TYPEREF>>	typesByName;
	private final UnidirectionalMap<TYPEREF, TYPEREF>									arrayTypes;
	private final UnidirectionalMap<REF, TYPEREF>										typesByRef;
	private final UnidirectionalMap<TYPEREF, StudentSideTypeDescription<TYPEREF>>		typeDescriptions;
	private final UnidirectionalMap<REF, TYPEREF>										typesHandledBySerdeses;

	public TypeCachingCommunicator(StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
			? extends TC, ? extends CM> communicator)
	{
		this.communicator = communicator;
		this.typesByName = UnidirectionalMap.builder().concurrent().build();
		this.arrayTypes = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
		this.typesByRef = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).weakKeys().build();
		this.typeDescriptions = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
		this.typesHandledBySerdeses = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).weakKeys().build();
	}

	@Override
	public OperationOutcome<TYPEREF, Void, TYPEREF> getTypeByName(String typeName)
	{
		return typesByName.computeIfAbsent(typeName, communicator::getTypeByName);
	}

	@Override
	public TYPEREF getArrayType(TYPEREF componentType)
	{
		return arrayTypes.computeIfAbsent(componentType, communicator::getArrayType);
	}

	@Override
	public TYPEREF getTypeOf(REF ref)
	{
		return typesByRef.computeIfAbsent(ref, communicator::getTypeOf);
	}

	@Override
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type)
	{
		return typeDescriptions.computeIfAbsent(type, communicator::describeType);
	}

	@Override
	public TYPEREF getTypeHandledBySerdes(REF serdesRef)
	{
		return typesHandledBySerdeses.computeIfAbsent(serdesRef, communicator::getTypeHandledBySerdes);
	}

	// Delegated methods

	@Override
	public boolean storeRefsIdentityBased()
	{
		return communicator.storeRefsIdentityBased();
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> createArray(TYPEREF arrayType, int length)
	{
		return communicator.createArray(arrayType, length);
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> createMultiArray(TYPEREF arrayType, List<Integer> dimensions)
	{
		return communicator.createMultiArray(arrayType, dimensions);
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> initializeArray(TYPEREF arrayType, List<REF> initialValues)
	{
		return communicator.initializeArray(arrayType, initialValues);
	}
	@Override
	public int getArrayLength(REF arrayRef)
	{
		return communicator.getArrayLength(arrayRef);
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> getArrayElement(REF arrayRef, int index)
	{
		return communicator.getArrayElement(arrayRef, index);
	}
	@Override
	public OperationOutcome<Void, Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		return communicator.setArrayElement(arrayRef, index, valueRef);
	}
	@Override
	public OperationOutcome<CONSTRUCTORREF, Void, TYPEREF> lookupConstructor(TYPEREF type, List<TYPEREF> params)
	{
		return communicator.lookupConstructor(type, params);
	}
	@Override
	public OperationOutcome<METHODREF, Void, TYPEREF> lookupMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, boolean isStatic)
	{
		return communicator.lookupMethod(type, name, returnType, params, isStatic);
	}
	@Override
	public OperationOutcome<FIELDREF, Void, TYPEREF> lookupField(TYPEREF type, String name, TYPEREF fieldType, boolean isStatic)
	{
		return communicator.lookupField(type, name, fieldType, isStatic);
	}
	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callConstructor(CONSTRUCTORREF constructor, List<REF> argRefs)
	{
		return communicator.callConstructor(constructor, argRefs);
	}
	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callStaticMethod(METHODREF method, List<REF> argRefs)
	{
		return communicator.callStaticMethod(method, argRefs);
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> getStaticField(FIELDREF field)
	{
		return communicator.getStaticField(field);
	}
	@Override
	public OperationOutcome<Void, Void, TYPEREF> setStaticField(FIELDREF field, REF valueRef)
	{
		return communicator.setStaticField(field, valueRef);
	}
	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callInstanceMethod(METHODREF method, REF receiverRef, List<REF> argRefs)
	{
		return communicator.callInstanceMethod(method, receiverRef, argRefs);
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> getInstanceField(FIELDREF field, REF receiverRef)
	{
		return communicator.getInstanceField(field, receiverRef);
	}
	@Override
	public OperationOutcome<Void, Void, TYPEREF> setInstanceField(FIELDREF field, REF receiverRef, REF valueRef)
	{
		return communicator.setInstanceField(field, receiverRef, valueRef);
	}
	@Override
	public TC getTransceiver()
	{
		return communicator.getTransceiver();
	}
	@Override
	public CM getCallbackManager()
	{
		return communicator.getCallbackManager();
	}
}
