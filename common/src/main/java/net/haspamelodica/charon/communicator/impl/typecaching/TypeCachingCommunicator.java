package net.haspamelodica.charon.communicator.impl.typecaching;

import java.util.List;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public class TypeCachingCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, TYPEREF, TC, CM>
{
	private final StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator;

	private final UnidirectionalMap<String, TYPEREF>								typesByName;
	private final UnidirectionalMap<TYPEREF, TYPEREF>								arrayTypes;
	private final UnidirectionalMap<REF, TYPEREF>									typesByRef;
	private final UnidirectionalMap<TYPEREF, StudentSideTypeDescription<TYPEREF>>	typeDescriptions;

	public TypeCachingCommunicator(StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator)
	{
		this.communicator = communicator;
		this.typesByName = UnidirectionalMap.builder().concurrent().build();
		this.arrayTypes = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
		this.typesByRef = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).weakKeys().build();
		this.typeDescriptions = UnidirectionalMap.builder().concurrent().identityMap(communicator.storeRefsIdentityBased()).build();
	}

	@Override
	public TYPEREF getTypeByName(String typeName)
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

	// Delegated methods

	@Override
	public boolean storeRefsIdentityBased()
	{
		return communicator.storeRefsIdentityBased();
	}
	@Override
	public REF newArray(TYPEREF componentType, int length)
	{
		return communicator.newArray(componentType, length);
	}
	@Override
	public REF newMultiArray(TYPEREF componentType, List<Integer> dimensions)
	{
		return communicator.newMultiArray(componentType, dimensions);
	}
	@Override
	public int getArrayLength(REF arrayRef)
	{
		return communicator.getArrayLength(arrayRef);
	}
	@Override
	public REF getArrayElement(REF arrayRef, int index)
	{
		return communicator.getArrayElement(arrayRef, index);
	}
	@Override
	public void setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		communicator.setArrayElement(arrayRef, index, valueRef);
	}
	@Override
	public RefOrError<REF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs)
	{
		return communicator.callConstructor(type, params, argRefs);
	}
	@Override
	public RefOrError<REF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs)
	{
		return communicator.callStaticMethod(type, name, returnType, params, argRefs);
	}
	@Override
	public REF getStaticField(TYPEREF type, String name, TYPEREF fieldType)
	{
		return communicator.getStaticField(type, name, fieldType);
	}
	@Override
	public void setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef)
	{
		communicator.setStaticField(type, name, fieldType, valueRef);
	}
	@Override
	public RefOrError<REF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs)
	{
		return communicator.callInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
	}
	@Override
	public REF getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef)
	{
		return communicator.getInstanceField(type, name, fieldType, receiverRef);
	}
	@Override
	public void setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef)
	{
		communicator.setInstanceField(type, name, fieldType, receiverRef, valueRef);
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
