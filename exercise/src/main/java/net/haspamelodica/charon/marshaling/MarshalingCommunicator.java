package net.haspamelodica.charon.marshaling;

import java.util.List;
import java.util.stream.Stream;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.exceptions.StudentSideException;
import net.haspamelodica.charon.impl.StudentSideImplUtils.StudentSideType;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks.CallbackMethod;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;

public class MarshalingCommunicator<REF>
{
	private final StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>>	communicator;
	private final Marshaler<REF>																								marshaler;

	public <M> MarshalingCommunicator(
			UninitializedStudentSideCommunicator<REF, ClientSideTransceiver<REF>, InternalCallbackManager<REF>> communicator,
			MarshalingCommunicatorCallbacks<REF, M> callbacks, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator.initialize(new StudentSideCommunicatorCallbacks<>()
		{
			@Override
			public String getCallbackInterfaceCn(REF ref)
			{
				return callbacks.getCallbackInterfaceCn(marshaler.translateFrom(ref));
			}

			@Override
			public RefOrError<REF> callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
					REF receiverRef, List<REF> argRefs)
			{
				Object receiverObj = marshaler.translateTo(receiverRef);

				CallbackMethod<M> callbackMethod = callbacks.lookupCallbackInstanceMethod(cn, name, returnClassname, params, receiverObj);
				Marshaler<REF> marshalerWithAdditionalSerdeses = marshaler.withAdditionalSerDeses(callbackMethod.additionalSerdeses());

				List<Object> argObjs = marshalerWithAdditionalSerdeses.receive(callbackMethod.paramTypes(), argRefs);

				Object resultObj;
				try
				{
					resultObj = callbacks.callCallbackInstanceMethodChecked(callbackMethod, receiverObj, argObjs);
				} catch(ExceptionInTargetException e)
				{
					//TODO transmit exception somehow
					return RefOrError.error(null);
				}

				return RefOrError.success(marshalerWithAdditionalSerdeses.send(callbackMethod.returnType(), resultObj));
			}
		});
		this.marshaler = new Marshaler<>(this.communicator, callbacks, serdesClasses);
	}
	public MarshalingCommunicator(
			StudentSideCommunicator<REF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator,
			Marshaler<REF> marshaler)
	{
		this.communicator = communicator;
		this.marshaler = marshaler;
	}

	public MarshalingCommunicator<REF> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serDeses)
	{
		return new MarshalingCommunicator<>(communicator, marshaler.withAdditionalSerDeses(serDeses));
	}

	public String getClassname(Object representationObject)
	{
		return communicator.getClassname(marshaler.translateFrom(representationObject));
	}
	public String getSuperclass(String cn)
	{
		return communicator.getSuperclass(cn);
	}
	public List<String> getInterfaces(String cn)
	{
		return communicator.getInterfaces(cn);
	}

	public <T> T callConstructor(StudentSideType<T> type, List<StudentSideType<?>> params, List<Object> args) throws StudentSideException
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);

		return marshaler.receiveOrThrow(type.localType(), resultRef);
	}
	// Neccessary for the Mockclasses frontend
	public REF callConstructorExistingRepresentationObject(StudentSideType<?> type, List<StudentSideType<?>> params, List<Object> args,
			Object representationObject) throws StudentSideException
	{
		RefOrError<REF> resultRef = callConstructorRawRef(type, params, args);
		marshaler.throwIfError(resultRef);

		marshaler.setRepresentationObjectRefPair(resultRef.resultOrErrorRef(), representationObject);
		return resultRef.resultOrErrorRef();
	}
	private RefOrError<REF> callConstructorRawRef(StudentSideType<?> type, List<StudentSideType<?>> params, List<Object> args)
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callConstructor(type.studentSideCN(), extractStudentSideCNs(params), argRefs);

		return resultRef;
	}

	public <T> T callStaticMethod(StudentSideType<?> type, String name, StudentSideType<T> returnType, List<StudentSideType<?>> params,
			List<Object> args) throws StudentSideException
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callStaticMethod(type.studentSideCN(), name, returnType.studentSideCN(), extractStudentSideCNs(params), argRefs);

		return marshaler.receiveOrThrow(returnType.localType(), resultRef);
	}

	public <T> T getStaticField(StudentSideType<?> type, String name, StudentSideType<T> fieldType)
	{
		REF resultRef = communicator.getStaticField(type.studentSideCN(), name, fieldType.studentSideCN());

		return marshaler.receive(fieldType.localType(), resultRef);
	}

	public <T> void setStaticField(StudentSideType<?> type, String name, StudentSideType<T> fieldType, T value)
	{
		REF valRef = marshaler.send(fieldType.localType(), value);

		communicator.setStaticField(type.studentSideCN(), name, fieldType.studentSideCN(), valRef);
	}

	public <T> T callInstanceMethod(StudentSideType<?> type, String name, StudentSideType<T> returnType, List<StudentSideType<?>> params,
			Object receiver, List<Object> args) throws StudentSideException
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);

		return callInstanceMethodRawReceiver(type, name, returnType, params, receiverRef, args);
	}
	// Neccessary for the Mockclasses frontend
	public <T> T callInstanceMethodRawReceiver(StudentSideType<?> type, String name, StudentSideType<T> returnType, List<StudentSideType<?>> params,
			REF receiverRef, List<Object> args) throws StudentSideException
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		RefOrError<REF> resultRef = communicator.callInstanceMethod(type.studentSideCN(), name, returnType.studentSideCN(), extractStudentSideCNs(params),
				receiverRef, argRefs);

		return marshaler.receiveOrThrow(returnType.localType(), resultRef);
	}

	public <T> T getInstanceField(StudentSideType<?> type, String name, StudentSideType<T> fieldType, Object receiver)
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);

		REF resultRef = communicator.getInstanceField(type.studentSideCN(), name, fieldType.studentSideCN(), receiverRef);

		return marshaler.receive(fieldType.localType(), resultRef);
	}

	public <T> void setInstanceField(StudentSideType<?> type, String name, StudentSideType<T> fieldType, Object receiver, T value)
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);
		REF valRef = marshaler.send(fieldType.localType(), value);

		communicator.setInstanceField(type.studentSideCN(), name, fieldType.studentSideCN(), receiverRef, valRef);
	}

	private List<Class<?>> extractLocalTypes(List<StudentSideType<?>> params)
	{
		// Java type system weirdness requires this to be a variable. Not even a cast works for some reason.
		Stream<Class<?>> mapped = params.stream().map(StudentSideType::localType);
		return mapped.toList();
	}
	private List<String> extractStudentSideCNs(List<StudentSideType<?>> params)
	{
		return params.stream().map(StudentSideType::studentSideCN).toList();
	}
}
