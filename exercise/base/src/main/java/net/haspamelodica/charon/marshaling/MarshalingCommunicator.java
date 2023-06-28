package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.OperationKind.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.OperationKind.CALL_METHOD;
import static net.haspamelodica.charon.OperationKind.CREATE_ARRAY;
import static net.haspamelodica.charon.OperationKind.CREATE_MULTI_ARRAY;
import static net.haspamelodica.charon.OperationKind.GET_ARRAY_ELEMENT;
import static net.haspamelodica.charon.OperationKind.GET_FIELD;
import static net.haspamelodica.charon.OperationKind.GET_TYPE_BY_NAME;
import static net.haspamelodica.charon.OperationKind.INITIALIZE_ARRAY;
import static net.haspamelodica.charon.OperationKind.LOOKUP_CONSTRUCTOR;
import static net.haspamelodica.charon.OperationKind.LOOKUP_FIELD;
import static net.haspamelodica.charon.OperationKind.LOOKUP_METHOD;
import static net.haspamelodica.charon.OperationKind.SET_FIELD;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.util.Collections;
import java.util.List;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;

public class MarshalingCommunicator<REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		SSX extends StudentSideCausedException>
{
	private final MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks;

	private final StudentSideCommunicator<REF, ?, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
			? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator;

	private final Marshaler<REF, TYPEREF, SSX> marshaler;

	public MarshalingCommunicator(
			UninitializedStudentSideCommunicator<REF, ?, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator,
			MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.callbacks = callbacks;
		this.communicator = createCallbacksAndInitialize(communicator, callbacks);
		this.marshaler = new Marshaler<>(createMarshalerCallbacks(callbacks), this.communicator, serdesClasses);
	}

	// Extracted to method to capture THROWABLEREF
	private <THROWABLEREF extends REF>
			StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>
			createCallbacksAndInitialize(
					UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator,
					MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks)
	{
		return communicator.initialize(createStudentSideCommunicatorCallbacks(callbacks));
	}

	private <M, THROWABLEREF extends REF, SST> StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF>
			createStudentSideCommunicatorCallbacks(MarshalingCommunicatorCallbacks<REF, TYPEREF, M, SST, SSX> callbacks)
	{
		return new StudentSideCommunicatorCallbacks<>()
		{
			@Override
			public String getCallbackInterfaceCn(REF ref)
			{
				return callbacks.getCallbackInterfaceCn(marshaler.translateTo(ref));
			}

			@Override
			public CallbackOperationOutcome<REF, THROWABLEREF> callCallbackInstanceMethod(
					TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
					REF receiverRef, List<REF> argRefs)
			{
				Object receiverObj = marshaler.translateTo(receiverRef);

				//TODO cache looked up callback methods
				CallbackMethod<M> callbackMethod = callbacks.lookupCallbackInstanceMethod(type, name, returnType, params, receiverObj.getClass());
				Marshaler<REF, TYPEREF, SSX> marshalerWithAdditionalSerdeses = marshaler.withAdditionalSerDeses(callbackMethod.additionalSerdeses());

				List<Object> argObjs = marshalerWithAdditionalSerdeses.receive(callbackMethod.params(), argRefs);

				CallbackOperationOutcome<Object, SST> resultOutcome = callbacks.callCallbackInstanceMethodChecked(
						callbackMethod.methodData(), receiverObj, argObjs);

				//TODO replace with pattern matching switch once that's in Java
				return switch(resultOutcome.kind())
				{
					case CALLBACK_RESULT -> new CallbackOperationOutcome.Result<>(marshalerWithAdditionalSerdeses.send(callbackMethod.returnType(),
							((CallbackOperationOutcome.Result<Object, SST>) resultOutcome).returnValue()));
					case CALLBACK_HIDDEN_ERROR -> new CallbackOperationOutcome.HiddenError<>();
					case CALLBACK_THROWN ->
					{
						@SuppressWarnings("unchecked") // responsibility of frontend
						THROWABLEREF thrownThrowableCasted = (THROWABLEREF) marshaler.translateFrom(
								((CallbackOperationOutcome.Thrown<Object, SST>) resultOutcome).thrownThrowable());
						yield new CallbackOperationOutcome.Thrown<>(thrownThrowableCasted);
					}
				};
			}
		};
	}

	private <SST> MarshalerCallbacks<REF, TYPEREF, SST, SSX>
			createMarshalerCallbacks(MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, SST, SSX> callbacks)
	{
		return new MarshalerCallbacks<>()
		{
			@Override
			public Object createForwardRef(UntranslatedRef<REF, TYPEREF> untranslatedRef)
			{
				return callbacks.createRepresentationObject(untranslatedRef);
			}

			@Override
			public String getCallbackInterfaceCn(Object translatedRef)
			{
				return callbacks.getCallbackInterfaceCn(translatedRef);
			}

			@Override
			public SST checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject)
			{
				return callbacks.checkRepresentsStudentSideThrowableAndCastOrNull(representationObject);
			}

			@Override
			public SSX newStudentCausedException(SST studentSideThrowable)
			{
				return callbacks.createStudentCausedException(studentSideThrowable);
			}
		};
	}

	private MarshalingCommunicator(
			MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks,
			StudentSideCommunicator<REF, ?, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator,
			Marshaler<REF, TYPEREF, SSX> marshaler)
	{
		this.callbacks = callbacks;
		this.communicator = communicator;
		this.marshaler = marshaler;
	}

	public MarshalingCommunicator<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, SSX>
			withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serDeses)
	{
		return new MarshalingCommunicator<>(callbacks, communicator, marshaler.withAdditionalSerDeses(serDeses));
	}

	public TYPEREF getTypeByNameAndVerify(String typeName)
	{
		TYPEREF result = Marshaler.handleOperationOutcomeVoid(GET_TYPE_BY_NAME, communicator.getTypeByName(typeName));

		String actualName = describeType(result).name();
		if(!actualName.equals(typeName))
			throw new FrameworkCausedException("Name of type created by name mismatched: expected " + typeName + ", but was " + actualName);

		return result;
	}

	public TYPEREF getArrayType(TYPEREF componentType)
	{
		return communicator.getArrayType(componentType);
	}

	public TYPEREF getTypeOf(Object representationObject)
	{
		return communicator.getTypeOf(marshaler.translateFrom(representationObject));
	}

	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type)
	{
		return communicator.describeType(type);
	}

	public TYPEREF getTypeHandledByStudentSideSerdes(Class<? extends SerDes<?>> serdesClass)
	{
		return marshaler.getTypeHandledByStudentSideSerdes(serdesClass);
	}

	public <T> T newArray(Class<T> arrayType, int length)
	{
		OperationOutcome<REF, Void, TYPEREF> resultRef = communicator.createArray(lookupCorrespondingStudentSideTypeOrThrow(arrayType), length);

		return marshaler.receiveOrThrowVoid(CREATE_ARRAY, arrayType, resultRef);
	}

	public <T> T newMultiArray(Class<T> arrayType, List<Integer> dimensions)
	{
		OperationOutcome<REF, Void, TYPEREF> resultRef = communicator.createMultiArray(lookupCorrespondingStudentSideTypeOrThrow(arrayType), dimensions);

		return marshaler.receiveOrThrowVoid(CREATE_MULTI_ARRAY, arrayType, resultRef);
	}

	public <T> T newArrayWithInitialValues(Class<T> arrayType, Class<?> initialValuesType, List<?> initialValues)
	{
		List<REF> initialValuesRefs = marshaler.send(Collections.nCopies(initialValues.size(), initialValuesType), initialValues);

		OperationOutcome<REF, Void, TYPEREF> resultRef = communicator.initializeArray(lookupCorrespondingStudentSideTypeOrThrow(arrayType), initialValuesRefs);

		return marshaler.receiveOrThrowVoid(INITIALIZE_ARRAY, arrayType, resultRef);
	}

	public int getArrayLength(Class<?> arrayType, Object array)
	{
		REF arrayRef = marshaler.send(arrayType, array);

		return communicator.getArrayLength(arrayRef);
	}

	public <T> T getArrayElement(Class<?> arrayType, Class<T> valueType, Object array, int index)
	{
		REF arrayRef = marshaler.send(arrayType, array);

		OperationOutcome<REF, Void, TYPEREF> valueRef = communicator.getArrayElement(arrayRef, index);

		return marshaler.receiveOrThrowVoid(GET_ARRAY_ELEMENT, valueType, valueRef);
	}

	public void setArrayElement(Class<?> arrayType, Class<?> valueType, Object array, int index, Object value)
	{
		REF arrayRef = marshaler.send(arrayType, array);
		REF valueRef = marshaler.send(valueType, value);

		communicator.setArrayElement(arrayRef, index, valueRef);
	}

	public CONSTRUCTORREF lookupConstructor(Class<?> type, List<Class<?>> params)
	{
		return Marshaler.handleOperationOutcomeVoid(LOOKUP_CONSTRUCTOR, communicator.lookupConstructor(
				lookupCorrespondingStudentSideTypeOrThrow(type), lookupCorrespondingStudentSideTypesOrThrow(params)));
	}

	public METHODREF lookupMethod(Class<?> type, String name, Class<?> returnType, List<Class<?>> params, boolean isStatic)
	{
		return Marshaler.handleOperationOutcomeVoid(LOOKUP_METHOD, communicator.lookupMethod(
				lookupCorrespondingStudentSideTypeOrThrow(type), name,
				lookupCorrespondingStudentSideTypeOrThrow(returnType), lookupCorrespondingStudentSideTypesOrThrow(params), isStatic));
	}

	public FIELDREF lookupField(Class<?> type, String name, Class<?> fieldType, boolean isStatic)
	{
		return Marshaler.handleOperationOutcomeVoid(LOOKUP_FIELD, communicator.lookupField(
				lookupCorrespondingStudentSideTypeOrThrow(type), name,
				lookupCorrespondingStudentSideTypeOrThrow(fieldType), isStatic));
	}

	public <T> T callConstructor(CONSTRUCTORREF constructor, Class<T> type, List<Class<?>> params, List<?> args) throws SSX
	{
		OperationOutcome<REF, ? extends REF, TYPEREF> resultRef = callConstructorRawRef(constructor, params, args);

		return marshaler.receiveOrThrow(CALL_CONSTRUCTOR, type, resultRef);
	}
	// Necessary for the Mockclasses frontend
	public REF callConstructorExistingRepresentationObject(CONSTRUCTORREF constructor, List<Class<?>> params,
			List<?> args, Object representationObject) throws SSX
	{
		REF resultRef = marshaler.handleOperationOutcome(CALL_CONSTRUCTOR, callConstructorRawRef(constructor, params, args));

		//TODO is this forward or backward?
		marshaler.setRepresentationObjectRefPairForward(resultRef, representationObject);
		return resultRef;
	}
	private OperationOutcome<REF, ? extends REF, TYPEREF> callConstructorRawRef(CONSTRUCTORREF constructor, List<Class<?>> params, List<?> args)
	{
		List<REF> argRefs = marshaler.send(params, args);

		return communicator.callConstructor(constructor, argRefs);
	}

	public <T> T callStaticMethod(METHODREF method, Class<T> returnType, List<Class<?>> params, List<?> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		OperationOutcome<REF, ? extends REF, TYPEREF> resultRef = communicator.callStaticMethod(method, argRefs);

		return marshaler.receiveOrThrow(CALL_METHOD, returnType, resultRef);
	}

	public <T> T getStaticField(FIELDREF field, Class<T> fieldType)
	{
		OperationOutcome<REF, Void, TYPEREF> resultRef = communicator.getStaticField(field);

		return marshaler.receiveOrThrowVoid(GET_FIELD, fieldType, resultRef);
	}

	public <T> void setStaticField(FIELDREF field, Class<T> fieldType, T value)
	{
		REF valRef = marshaler.send(fieldType, value);

		OperationOutcome<Void, Void, TYPEREF> outcome = communicator.setStaticField(field, valRef);

		Marshaler.handleOperationOutcomeVoid(SET_FIELD, outcome);
	}

	public <T> T callInstanceMethod(METHODREF method, Class<?> type, Class<T> returnType,
			List<Class<?>> params, Object receiver, List<?> args) throws SSX
	{
		REF receiverRef = marshaler.send(type, receiver);

		return callInstanceMethodRawReceiver(method, returnType, params, receiverRef, args);
	}
	// Necessary for the Mockclasses frontend
	public <T> T callInstanceMethodRawReceiver(METHODREF method, Class<T> returnType,
			List<Class<?>> params, REF receiverRef, List<?> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		OperationOutcome<REF, ? extends REF, TYPEREF> resultRef = communicator.callInstanceMethod(method, receiverRef, argRefs);

		return marshaler.receiveOrThrow(CALL_METHOD, returnType, resultRef);
	}

	public <T> T getInstanceField(FIELDREF field, Class<?> type, Class<T> fieldType, Object receiver)
	{
		REF receiverRef = marshaler.send(type, receiver);

		OperationOutcome<REF, Void, TYPEREF> resultRef = communicator.getInstanceField(field, receiverRef);

		return marshaler.receiveOrThrowVoid(GET_FIELD, fieldType, resultRef);
	}

	public <T> void setInstanceField(FIELDREF field, Class<?> type, Class<T> fieldType, Object receiver, T value)
	{
		REF receiverRef = marshaler.send(type, receiver);
		REF valRef = marshaler.send(fieldType, value);

		OperationOutcome<Void, Void, TYPEREF> outcome = communicator.setInstanceField(field, receiverRef, valRef);

		Marshaler.handleOperationOutcomeVoid(SET_FIELD, outcome);
	}

	public <T> T sendAndReceive(Class<T> type, Object receiver)
	{
		return marshaler.receive(type, marshaler.send(type, receiver));
	}

	private List<TYPEREF> lookupCorrespondingStudentSideTypesOrThrow(List<Class<?>> types)
	{
		return types.stream().map(this::lookupCorrespondingStudentSideTypeOrThrow).toList();
	}
	public TYPEREF lookupCorrespondingStudentSideTypeOrThrow(Class<?> serializableOrRepresentationClass)
	{
		return lookupCorrespondingStudentSideType(serializableOrRepresentationClass, true);
	}
	public TYPEREF lookupCorrespondingStudentSideTypeOrNull(Class<?> serializableOrRepresentationClass)
	{
		return lookupCorrespondingStudentSideType(serializableOrRepresentationClass, false);
	}
	public TYPEREF lookupCorrespondingStudentSideType(Class<?> serializableOrRepresentationClass, boolean throwIfNotFound)
	{
		if(marshaler.isSerializedType(serializableOrRepresentationClass))
			return getTypeByNameAndVerify(classToName(serializableOrRepresentationClass));

		return callbacks.lookupCorrespondingStudentSideTypeForRepresentationClass((Class<?>) serializableOrRepresentationClass, throwIfNotFound);
	}

	public REF getRawRef(Object representationObject)
	{
		return marshaler.translateFrom(representationObject);
	}
}
