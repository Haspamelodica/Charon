package net.haspamelodica.charon.marshaling;

import java.util.List;
import java.util.stream.Stream;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedTyperef;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
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
				return callbacks.getCallbackInterfaceCn(marshaler.translateFrom(ref));
			}

			@Override
			public RefOrError<REF> callCallbackInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
					REF receiverRef, List<REF> argRefs)
			{
				Object receiverObj = marshaler.translateTo(receiverRef);

				StudentSideType<TYPEREF, ?> studentSideReceiverType = lookupStudentSideType(type);
				StudentSideType<TYPEREF, ?> studentSideReturnType = lookupStudentSideType(returnType);
				List<StudentSideType<TYPEREF, ?>> studentSideParams = lookupStudentSideTypes(params);

				CallbackMethod<M> callbackMethod = callbacks.lookupCallbackInstanceMethod(
						studentSideReceiverType, receiverObj.getClass(), name, studentSideReturnType, studentSideParams);
				Marshaler<REF, TYPEREF, SSX> marshalerWithAdditionalSerdeses = marshaler.withAdditionalSerDeses(callbackMethod.additionalSerdeses());

				List<Object> argObjs = marshalerWithAdditionalSerdeses.receive(extractLocalTypes(studentSideParams), argRefs);

				Object resultObj;
				try
				{
					resultObj = callbacks.callCallbackInstanceMethodChecked(callbackMethod.methodData(), receiverObj, argObjs);
				} catch(ExceptionInTargetException e)
				{
					//TODO transmit exception somehow
					return RefOrError.error(null);
				}

				return RefOrError.success(marshalerWithAdditionalSerdeses.send(studentSideReturnType.localType(), resultObj));
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
				return callbacks.createRepresentationObject(lookupStudentSideType(untranslatedRef.getType()), untranslatedRef);
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
				return callbacks.newStudentCausedException(studentSideThrowable);
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

	public <T> StudentSideType<TYPEREF, T> lookupStudentSideType(Class<T> clazz, String studentSideCN)
	{
		return new StudentSideType<>(clazz, studentSideCN, communicator.getTypeByName(studentSideCN));
	}

	public StudentSideType<TYPEREF, ?> getTypeOf(Object representationObject)
	{
		return lookupStudentSideType(communicator.getTypeOf(marshaler.translateFrom(representationObject)));
	}

	public StudentSideTypeDescription<StudentSideType<TYPEREF, ?>> describeType(StudentSideType<TYPEREF, ?> type)
	{
		StudentSideTypeDescription<TYPEREF> result = communicator.describeType(type.studentSideType());
		if(!result.name().equals(type.studentSideCN()))
			throw new FrameworkCausedException("Type description name mismatches: expected " + type.studentSideCN() + ", but was " + result.name());

		return new StudentSideTypeDescription<>(
				result.kind(),
				result.name(),
				result.superclass().map(this::lookupStudentSideType),
				lookupStudentSideTypes(result.superinterfaces()),
				result.componentTypeIfArray().map(this::lookupStudentSideType));
	}

	public <T> T callConstructor(StudentSideType<TYPEREF, T> type, List<StudentSideType<TYPEREF, ?>> params, List<Object> args) throws SSX
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);

		return marshaler.receiveOrThrow(type.localType(), resultRef);
	}
	// Neccessary for the Mockclasses frontend
	public REF callConstructorExistingRepresentationObject(StudentSideType<TYPEREF, ?> type, List<StudentSideType<TYPEREF, ?>> params,
			List<Object> args, Object representationObject) throws SSX
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);
		marshaler.throwIfError(resultRef);

		marshaler.setRepresentationObjectRefPair(resultRef.resultOrErrorRef(), representationObject);
		return resultRef.resultOrErrorRef();
	}
	private RefOrError<REF> callConstructorRawRef(StudentSideType<TYPEREF, ?> type, List<StudentSideType<TYPEREF, ?>> params, List<Object> args)
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callConstructor(type.studentSideType(), extractStudentSideTypes(params), argRefs);

		return resultRef;
	}

	public <T> T callStaticMethod(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> returnType, List<StudentSideType<TYPEREF, ?>> params,
			List<Object> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callStaticMethod(type.studentSideType(), name, returnType.studentSideType(), extractStudentSideTypes(params), argRefs);

		return marshaler.receiveOrThrow(returnType.localType(), resultRef);
	}

	public <T> T getStaticField(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> fieldType)
	{
		REF resultRef = communicator.getStaticField(type.studentSideType(), name, fieldType.studentSideType());

		return marshaler.receive(fieldType.localType(), resultRef);
	}

	public <T> void setStaticField(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> fieldType, T value)
	{
		REF valRef = marshaler.send(fieldType.localType(), value);

		communicator.setStaticField(type.studentSideType(), name, fieldType.studentSideType(), valRef);
	}

	public <T> T callInstanceMethod(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> returnType,
			List<StudentSideType<TYPEREF, ?>> params, Object receiver, List<Object> args) throws SSX
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);

		return callInstanceMethodRawReceiver(type, name, returnType, params, receiverRef, args);
	}
	// Neccessary for the Mockclasses frontend
	public <T> T callInstanceMethodRawReceiver(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> returnType,
			List<StudentSideType<TYPEREF, ?>> params, REF receiverRef, List<Object> args) throws SSX
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callInstanceMethod(type.studentSideType(), name, returnType.studentSideType(), extractStudentSideTypes(params),
				receiverRef, argRefs);

		return marshaler.receiveOrThrow(returnType.localType(), resultRef);
	}

	public <T> T getInstanceField(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> fieldType, Object receiver)
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);

		REF resultRef = communicator.getInstanceField(type.studentSideType(), name, fieldType.studentSideType(), receiverRef);

		return marshaler.receive(fieldType.localType(), resultRef);
	}

	public <T> void setInstanceField(StudentSideType<TYPEREF, ?> type, String name, StudentSideType<TYPEREF, T> fieldType, Object receiver, T value)
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);
		REF valRef = marshaler.send(fieldType.localType(), value);

		communicator.setInstanceField(type.studentSideType(), name, fieldType.studentSideType(), receiverRef, valRef);
	}

	private List<Class<?>> extractLocalTypes(List<StudentSideType<TYPEREF, ?>> types)
	{
		// Java type system weirdness requires this to be a variable. Not even a cast works for some reason.
		Stream<Class<?>> mapped = types.stream().map(StudentSideType::localType);
		return mapped.toList();
	}
	private List<TYPEREF> extractStudentSideTypes(List<StudentSideType<TYPEREF, ?>> types)
	{
		return types.stream().map(StudentSideType::studentSideType).toList();
	}

	private List<StudentSideType<TYPEREF, ?>> lookupStudentSideTypes(List<TYPEREF> types)
	{
		// Java type system weirdness requires this to be a variable. Not even a cast works for some reason.
		Stream<StudentSideType<TYPEREF, ?>> mapped = types.stream().map(this::lookupStudentSideType);
		return mapped.toList();
	}
	private StudentSideType<TYPEREF, ?> lookupStudentSideType(TYPEREF type)
	{
		return lookupStudentSideType(new UntranslatedTyperef<>(communicator, type));
	}
	private StudentSideType<TYPEREF, ?> lookupStudentSideType(UntranslatedTyperef<REF, TYPEREF> untranslatedTyperef)
	{
		Class<?> localType = callbacks.lookupLocalType(untranslatedTyperef);
		return new StudentSideType<>(localType, untranslatedTyperef.describe().name(), untranslatedTyperef.typeref());
	}
}
