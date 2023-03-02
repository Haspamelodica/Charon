package net.haspamelodica.charon.marshaling;

import java.util.List;
import java.util.stream.Stream;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.impl.StudentSideImplUtils.StudentSideType;

public class MarshalingCommunicator<REF>
{
	private final StudentSideCommunicatorClientSide<REF>	communicator;
	private final Marshaler<REF>							marshaler;

	public MarshalingCommunicator(StudentSideCommunicatorClientSide<REF> communicator,
			RepresentationObjectMarshaler representationObjectMarshaler, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this(communicator, new Marshaler<>(communicator, representationObjectMarshaler, serdesClasses));
	}
	public MarshalingCommunicator(StudentSideCommunicatorClientSide<REF> communicator, Marshaler<REF> marshaler)
	{
		this.communicator = communicator;
		this.marshaler = marshaler;
	}

	public MarshalingCommunicator<REF> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serDeses)
	{
		return new MarshalingCommunicator<>(communicator, marshaler.withAdditionalSerDeses(serDeses));
	}

	public <T> T callConstructor(StudentSideType<T> type, List<StudentSideType<?>> params, List<Object> args)
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		REF resultRef = communicator.callConstructor(type.studentSideCN(), extractStudentSideCNs(params), argRefs);

		return marshaler.receive(type.localType(), resultRef);
	}

	public <T> T callStaticMethod(StudentSideType<?> type, String name, StudentSideType<T> returnType, List<StudentSideType<?>> params, List<Object> args)
	{
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		REF resultRef = communicator.callStaticMethod(type.studentSideCN(), name, returnType.studentSideCN(), extractStudentSideCNs(params), argRefs);

		return marshaler.receive(returnType.localType(), resultRef);
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
			Object receiver, List<Object> args)
	{
		REF receiverRef = marshaler.send(type.localType(), receiver);
		List<REF> argRefs = marshaler.send(extractLocalTypes(params), args);

		REF resultRef = communicator.callInstanceMethod(type.studentSideCN(), name, returnType.studentSideCN(), extractStudentSideCNs(params),
				receiverRef, argRefs);

		return marshaler.receive(returnType.localType(), resultRef);
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

	public Object createCallbackInstance(String interfaceName, Callback<Object> callback)
	{
		// TODO Auto-generated method stub
		return null;
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
