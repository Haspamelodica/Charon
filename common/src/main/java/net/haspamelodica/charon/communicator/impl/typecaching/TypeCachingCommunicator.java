package net.haspamelodica.charon.communicator.impl.typecaching;

import java.util.List;

import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public class TypeCachingCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, TYPEREF, TC, CM>
{
	private final StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator;

	private final UnidirectionalMap<String, OperationOutcome<REF, TYPEREF>>			typesByName;
	private final UnidirectionalMap<TYPEREF, TYPEREF>								arrayTypes;
	private final UnidirectionalMap<REF, TYPEREF>									typesByRef;
	private final UnidirectionalMap<TYPEREF, StudentSideTypeDescription<TYPEREF>>	typeDescriptions;
	private final UnidirectionalMap<REF, TYPEREF>									typesHandledBySerdeses;

	public TypeCachingCommunicator(StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator)
	{
		this.communicator = communicator;
		this.typesByName = UnidirectionalMap.builder().concurrent().build();
		this.arrayTypes = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
		this.typesByRef = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).weakKeys().build();
		this.typeDescriptions = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
		this.typesHandledBySerdeses = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).weakKeys().build();
	}

	@Override
	public OperationOutcome<REF, TYPEREF> getTypeByName(String typeName)
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
	public OperationOutcome<REF, TYPEREF> newArray(TYPEREF arrayType, int length)
	{
		return communicator.newArray(arrayType, length);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> newMultiArray(TYPEREF arrayType, List<Integer> dimensions)
	{
		return communicator.newMultiArray(arrayType, dimensions);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> newArrayWithInitialValues(TYPEREF arrayType, List<REF> initialValues)
	{
		return communicator.newArrayWithInitialValues(arrayType, initialValues);
	}
	@Override
	public int getArrayLength(REF arrayRef)
	{
		return communicator.getArrayLength(arrayRef);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> getArrayElement(REF arrayRef, int index)
	{
		return communicator.getArrayElement(arrayRef, index);
	}
	@Override
	public OperationOutcome<Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		return communicator.setArrayElement(arrayRef, index, valueRef);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs)
	{
		return communicator.callConstructor(type, params, argRefs);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs)
	{
		return communicator.callStaticMethod(type, name, returnType, params, argRefs);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> getStaticField(TYPEREF type, String name, TYPEREF fieldType)
	{
		return communicator.getStaticField(type, name, fieldType);
	}
	@Override
	public OperationOutcome<Void, TYPEREF> setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef)
	{
		return communicator.setStaticField(type, name, fieldType, valueRef);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs)
	{
		return communicator.callInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
	}
	@Override
	public OperationOutcome<REF, TYPEREF> getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef)
	{
		return communicator.getInstanceField(type, name, fieldType, receiverRef);
	}
	@Override
	public OperationOutcome<Void, TYPEREF> setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef)
	{
		return communicator.setInstanceField(type, name, fieldType, receiverRef, valueRef);
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
