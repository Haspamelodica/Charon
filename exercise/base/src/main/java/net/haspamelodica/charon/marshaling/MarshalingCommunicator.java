package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.OperationKind.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.OperationKind.CALL_METHOD;
import static net.haspamelodica.charon.OperationKind.CREATE_ARRAY;
import static net.haspamelodica.charon.OperationKind.CREATE_MULTI_ARRAY;
import static net.haspamelodica.charon.OperationKind.GET_ARRAY_ELEMENT;
import static net.haspamelodica.charon.OperationKind.GET_FIELD;
import static net.haspamelodica.charon.OperationKind.GET_TYPE_BY_NAME;
import static net.haspamelodica.charon.OperationKind.INITIALIZE_ARRAY;
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

public class MarshalingCommunicator<REF, TYPEREF extends REF, SSX extends StudentSideCausedException>
{
	private final MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks;

	private final StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator;

	private final Marshaler<REF, TYPEREF, SSX> marshaler;

	public MarshalingCommunicator(
			UninitializedStudentSideCommunicator<REF, TYPEREF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator,
			MarshalingCommunicatorCallbacks<REF, TYPEREF, ?, ?, SSX> callbacks, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.callbacks = callbacks;
		this.communicator = communicator.initialize(createStudentSideCommunicatorCallbacks(callbacks));
		this.marshaler = new Marshaler<>(createMarshalerCallbacks(callbacks), this.communicator, serdesClasses);
	}

	private <M, SST> StudentSideCommunicatorCallbacks<REF, TYPEREF>
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
			public CallbackOperationOutcome<REF, REF> callCallbackInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
					REF receiverRef, List<REF> argRefs)
			{
				Object receiverObj = marshaler.translateTo(receiverRef);

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
					case CALLBACK_THROWN -> new CallbackOperationOutcome.Thrown<>(marshaler.translateFrom(
							((CallbackOperationOutcome.Thrown<Object, SST>) resultOutcome).thrownThrowable()));
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
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator,
			Marshaler<REF, TYPEREF, SSX> marshaler)
	{
		this.callbacks = callbacks;
		this.communicator = communicator;
		this.marshaler = marshaler;
	}

	public MarshalingCommunicator<REF, TYPEREF, SSX> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serDeses)
	{
		return new MarshalingCommunicator<>(callbacks, communicator, marshaler.withAdditionalSerDeses(serDeses));
	}

	public TYPEREF getTypeByNameAndVerify(String typeName) throws SSX
	{
		//TODO eliminate this cast once Outcome has three type arguments
		@SuppressWarnings("unchecked")
		TYPEREF result = (TYPEREF) marshaler.handleOperationOutcome(GET_TYPE_BY_NAME, communicator.getTypeByName(typeName));

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
		OperationOutcome<REF, TYPEREF> resultRef = communicator.newArray(lookupCorrespondingStudentSideTypeOrThrow(arrayType), length);

		return marshaler.receiveOrThrow(CREATE_ARRAY, arrayType, resultRef);
	}

	public <T> T newMultiArray(Class<T> arrayType, List<Integer> dimensions)
	{
		OperationOutcome<REF, TYPEREF> resultRef = communicator.newMultiArray(lookupCorrespondingStudentSideTypeOrThrow(arrayType), dimensions);

		return marshaler.receiveOrThrow(CREATE_MULTI_ARRAY, arrayType, resultRef);
	}

	public <T> T newArrayWithInitialValues(Class<T> arrayType, Class<?> initialValuesType, List<?> initialValues)
	{
		List<REF> initialValuesRefs = marshaler.send(Collections.nCopies(initialValues.size(), initialValuesType), initialValues);

		OperationOutcome<REF, TYPEREF> resultRef = communicator.newArrayWithInitialValues(lookupCorrespondingStudentSideTypeOrThrow(arrayType), initialValuesRefs);

		return marshaler.receiveOrThrow(INITIALIZE_ARRAY, arrayType, resultRef);
	}

	public int getArrayLength(Class<?> arrayType, Object array)
	{
		REF arrayRef = marshaler.send(arrayType, array);

		return communicator.getArrayLength(arrayRef);
	}

	public <T> T getArrayElement(Class<?> arrayType, Class<T> valueType, Object array, int index)
	{
		REF arrayRef = marshaler.send(arrayType, array);

		OperationOutcome<REF, TYPEREF> valueRef = communicator.getArrayElement(arrayRef, index);

		return marshaler.receiveOrThrow(GET_ARRAY_ELEMENT, valueType, valueRef);
	}

	public void setArrayElement(Class<?> arrayType, Class<?> valueType, Object array, int index, Object value)
	{
		REF arrayRef = marshaler.send(arrayType, array);
		REF valueRef = marshaler.send(valueType, value);

		communicator.setArrayElement(arrayRef, index, valueRef);
	}

	public <T> T callConstructor(Class<T> type, List<Class<?>> params, List<?> args) throws SSX
	{
		OperationOutcome<REF, TYPEREF> resultRef = callConstructorRawRef(type, params, args);

		return marshaler.receiveOrThrow(CALL_CONSTRUCTOR, type, resultRef);
	}
	// Neccessary for the Mockclasses frontend
	public REF callConstructorExistingRepresentationObject(Class<?> type, List<Class<?>> params,
			List<?> args, Object representationObject) throws SSX
	{
		REF resultRef = marshaler.handleOperationOutcome(CALL_CONSTRUCTOR, callConstructorRawRef(type, params, args));

		marshaler.setRepresentationObjectRefPair(resultRef, representationObject);
		return resultRef;
	}
	private OperationOutcome<REF, TYPEREF> callConstructorRawRef(Class<?> type, List<Class<?>> params, List<?> args)
	{
		List<REF> argRefs = marshaler.send(params, args);

		return communicator.callConstructor(lookupCorrespondingStudentSideTypeOrThrow(type), lookupCorrespondingStudentSideTypesOrThrow(params), argRefs);
	}

	public <T> T callStaticMethod(Class<?> type, String name, Class<T> returnType, List<Class<?>> params, List<?> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		OperationOutcome<REF, TYPEREF> resultRef = communicator.callStaticMethod(
				lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(returnType),
				lookupCorrespondingStudentSideTypesOrThrow(params), argRefs);

		return marshaler.receiveOrThrow(CALL_METHOD, returnType, resultRef);
	}

	public <T> T getStaticField(Class<?> type, String name, Class<T> fieldType)
	{
		OperationOutcome<REF, TYPEREF> resultRef = communicator.getStaticField(
				lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType));

		return marshaler.receiveOrThrow(GET_FIELD, fieldType, resultRef);
	}

	public <T> void setStaticField(Class<?> type, String name, Class<T> fieldType, T value)
	{
		REF valRef = marshaler.send(fieldType, value);

		OperationOutcome<Void, TYPEREF> outcome = communicator.setStaticField(
				lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType), valRef);

		marshaler.handleOperationOutcomeVoid(SET_FIELD, outcome);
	}

	public <T> T callInstanceMethod(Class<?> type, String name, Class<T> returnType,
			List<Class<?>> params, Object receiver, List<?> args) throws SSX
	{
		REF receiverRef = marshaler.send(type, receiver);

		return callInstanceMethodRawReceiver(type, name, returnType, params, receiverRef, args);
	}
	// Neccessary for the Mockclasses frontend
	public <T> T callInstanceMethodRawReceiver(Class<?> type, String name, Class<T> returnType,
			List<Class<?>> params, REF receiverRef, List<?> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		OperationOutcome<REF, TYPEREF> resultRef = communicator.callInstanceMethod(
				lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(returnType),
				lookupCorrespondingStudentSideTypesOrThrow(params),
				receiverRef, argRefs);

		return marshaler.receiveOrThrow(CALL_METHOD, returnType, resultRef);
	}

	public <T> T getInstanceField(Class<?> type, String name, Class<T> fieldType, Object receiver)
	{
		REF receiverRef = marshaler.send(type, receiver);

		OperationOutcome<REF, TYPEREF> resultRef = communicator.getInstanceField(lookupCorrespondingStudentSideTypeOrThrow(type), name,
				lookupCorrespondingStudentSideTypeOrThrow(fieldType), receiverRef);

		return marshaler.receiveOrThrow(GET_FIELD, fieldType, resultRef);
	}

	public <T> void setInstanceField(Class<?> type, String name, Class<T> fieldType, Object receiver, T value)
	{
		REF receiverRef = marshaler.send(type, receiver);
		REF valRef = marshaler.send(fieldType, value);

		OperationOutcome<Void, TYPEREF> outcome = communicator.setInstanceField(
				lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType), receiverRef, valRef);

		marshaler.handleOperationOutcomeVoid(SET_FIELD, outcome);
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
