package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.util.List;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;

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

	private <M> StudentSideCommunicatorCallbacks<REF, TYPEREF>
			createStudentSideCommunicatorCallbacks(MarshalingCommunicatorCallbacks<REF, TYPEREF, M, ?, SSX> callbacks)
	{
		return new StudentSideCommunicatorCallbacks<>()
		{
			@Override
			public String getCallbackInterfaceCn(REF ref)
			{
				return callbacks.getCallbackInterfaceCn(marshaler.translateTo(ref));
			}

			@Override
			public RefOrError<REF> callCallbackInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
					REF receiverRef, List<REF> argRefs)
			{
				Object receiverObj = marshaler.translateTo(receiverRef);

				CallbackMethod<M> callbackMethod = callbacks.lookupCallbackInstanceMethod(type, name, returnType, params, receiverObj.getClass());
				Marshaler<REF, TYPEREF, SSX> marshalerWithAdditionalSerdeses = marshaler.withAdditionalSerDeses(callbackMethod.additionalSerdeses());

				List<Object> argObjs = marshalerWithAdditionalSerdeses.receive(callbackMethod.params(), argRefs);

				Object resultObj;
				try
				{
					resultObj = callbacks.callCallbackInstanceMethodChecked(callbackMethod.methodData(), receiverObj, argObjs);
				} catch(ExceptionInTargetException e)
				{
					//TODO transmit exception somehow, if it's a ForStudentException
					return RefOrError.error(null);
				}

				return RefOrError.success(marshalerWithAdditionalSerdeses.send(callbackMethod.returnType(), resultObj));
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

	public TYPEREF getTypeByName(String typeName)
	{
		return communicator.getTypeByName(typeName);
	}

	public TYPEREF getTypeOf(Object representationObject)
	{
		return communicator.getTypeOf(marshaler.translateFrom(representationObject));
	}

	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type)
	{
		return communicator.describeType(type);
	}

	public <T> T callConstructor(Class<T> type, List<Class<?>> params, List<Object> args) throws SSX
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);

		return marshaler.receiveOrThrow(type, resultRef);
	}
	// Neccessary for the Mockclasses frontend
	public REF callConstructorExistingRepresentationObject(Class<?> type, List<Class<?>> params,
			List<Object> args, Object representationObject) throws SSX
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);
		marshaler.throwIfError(resultRef);

		marshaler.setRepresentationObjectRefPair(resultRef.resultOrErrorRef(), representationObject);
		return resultRef.resultOrErrorRef();
	}
	private RefOrError<REF> callConstructorRawRef(Class<?> type, List<Class<?>> params, List<Object> args)
	{
		List<REF> argRefs = marshaler.send(params, args);

		RefOrError<REF> resultRef = communicator.callConstructor(lookupCorrespondingStudentSideTypeOrThrow(type), lookupCorrespondingStudentSideTypesOrThrow(params), argRefs);

		return resultRef;
	}

	public <T> T callStaticMethod(Class<?> type, String name, Class<T> returnType, List<Class<?>> params, List<Object> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		RefOrError<REF> resultRef = communicator.callStaticMethod(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(returnType), lookupCorrespondingStudentSideTypesOrThrow(params), argRefs);

		return marshaler.receiveOrThrow(returnType, resultRef);
	}

	public <T> T getStaticField(Class<?> type, String name, Class<T> fieldType)
	{
		REF resultRef = communicator.getStaticField(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType));

		return marshaler.receive(fieldType, resultRef);
	}

	public <T> void setStaticField(Class<?> type, String name, Class<T> fieldType, T value)
	{
		REF valRef = marshaler.send(fieldType, value);

		communicator.setStaticField(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType), valRef);
	}

	public <T> T callInstanceMethod(Class<?> type, String name, Class<T> returnType,
			List<Class<?>> params, Object receiver, List<Object> args) throws SSX
	{
		REF receiverRef = marshaler.send(type, receiver);

		return callInstanceMethodRawReceiver(type, name, returnType, params, receiverRef, args);
	}
	// Neccessary for the Mockclasses frontend
	public <T> T callInstanceMethodRawReceiver(Class<?> type, String name, Class<T> returnType,
			List<Class<?>> params, REF receiverRef, List<Object> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(params, args);

		RefOrError<REF> resultRef = communicator.callInstanceMethod(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(returnType), lookupCorrespondingStudentSideTypesOrThrow(params),
				receiverRef, argRefs);

		return marshaler.receiveOrThrow(returnType, resultRef);
	}

	public <T> T getInstanceField(Class<?> type, String name, Class<T> fieldType, Object receiver)
	{
		REF receiverRef = marshaler.send(type, receiver);

		REF resultRef = communicator.getInstanceField(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType), receiverRef);

		return marshaler.receive(fieldType, resultRef);
	}

	public <T> void setInstanceField(Class<?> type, String name, Class<T> fieldType, Object receiver, T value)
	{
		REF receiverRef = marshaler.send(type, receiver);
		REF valRef = marshaler.send(fieldType, value);

		communicator.setInstanceField(lookupCorrespondingStudentSideTypeOrThrow(type), name, lookupCorrespondingStudentSideTypeOrThrow(fieldType), receiverRef, valRef);
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
			return communicator.getTypeByName(classToName(serializableOrRepresentationClass));

		return callbacks.lookupCorrespondingStudentSideTypeForRepresentationClass(serializableOrRepresentationClass, throwIfNotFound);
	}
}
