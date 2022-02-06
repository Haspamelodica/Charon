package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.CALL_CONSTRUCTOR;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.CALL_INSTANCE_METHOD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.CALL_STATIC_METHOD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.GET_CLASSNAME;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.GET_INSTANCE_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.GET_STATIC_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.RECEIVE;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.SEND;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.SET_INSTANCE_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command.SET_STATIC_FIELD;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.CommunicationException;
import net.haspamelodica.studentcodeseparator.exceptions.StudentCodeSeparatorException;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DataCommunicator implements StudentSideCommunicator<IntRef>
{
	private final DataInputStream	rawIn;
	private final DataOutputStream	rawOut;

	private final IntRefManager refManager;

	public DataCommunicator(DataInputStream rawIn, DataOutputStream rawOut)
	{
		this.rawIn = rawIn;
		this.rawOut = rawOut;

		this.refManager = new IntRefManager();
	}

	@Override
	public String getStudentSideClassname(IntRef ref)
	{
		return executeCommand(GET_CLASSNAME, out -> out.writeInt(ref.ref()), DataInput::readUTF);
	}

	@Override
	public <T> IntRef send(Serializer<T> serializer, IntRef serializerRef, T obj)
	{
		return executeRefCommand(SEND, out ->
		{
			refManager.writeRef(out, serializerRef);
			serializer.serialize(out, obj);
		});
	}

	@Override
	public <T> T receive(Serializer<T> serializer, IntRef serializerRef, IntRef objRef)
	{
		return executeCommand(RECEIVE, out ->
		{
			refManager.writeRef(out, serializerRef);
			refManager.writeRef(out, objRef);
		}, serializer::deserialize);
	}

	@Override
	public IntRef callConstructor(String cn, List<String> params, List<IntRef> argRefs)
	{
		return executeRefCommand(CALL_CONSTRUCTOR, out ->
		{
			out.writeUTF(cn);
			writeArgs(out, params, argRefs);
		});
	}

	@Override
	public IntRef callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<IntRef> argRefs)
	{
		return executeRefCommand(CALL_STATIC_METHOD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(returnClassname);
			writeArgs(out, params, argRefs);
		});
	}
	@Override
	public IntRef getStaticField(String cn, String name, String fieldClassname)
	{
		return executeRefCommand(GET_STATIC_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
		});
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, IntRef valueRef)
	{
		executeVoidCommand(SET_STATIC_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			refManager.writeRef(out, valueRef);
		});
	}

	@Override
	public IntRef callInstanceMethod(String cn, String name, String returnClassname, List<String> params, IntRef receiverRef, List<IntRef> argRefs)
	{
		return executeRefCommand(CALL_INSTANCE_METHOD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(returnClassname);
			refManager.writeRef(out, receiverRef);
			writeArgs(out, params, argRefs);
		});
	}
	@Override
	public IntRef getInstanceField(String cn, String name, String fieldClassname, IntRef receiverRef)
	{
		return executeRefCommand(GET_INSTANCE_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			refManager.writeRef(out, receiverRef);
		});
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, IntRef receiverRef, IntRef valueRef)
	{
		executeVoidCommand(SET_INSTANCE_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			refManager.writeRef(out, receiverRef);
			refManager.writeRef(out, valueRef);
		});
	}

	private void executeVoidCommand(Command command, IOConsumer<DataOutput> sendParams)
	{
		executeCommand(command, sendParams, in -> null);
	}
	private IntRef executeRefCommand(Command command, IOConsumer<DataOutput> sendParams)
	{
		return executeCommand(command, sendParams, refManager::readRef);
	}
	private <R> R executeCommand(Command command, IOConsumer<DataOutput> sendParams, IOFunction<DataInput, R> parseResponse)
	{
		return executeCommand(command, sendParams, (in, out) -> parseResponse.apply(in));
	}
	private <R> R executeCommand(Command command, IOConsumer<DataOutput> sendParams, IOBiFunction<DataInput, DataOutput, R> parseResponse)
	{
		return executeCommand(command, (in, out) -> sendParams.accept(out), parseResponse);
	}
	private <R> R executeCommand(Command command, IOBiConsumer<DataInput, DataOutput> sendParams, IOBiFunction<DataInput, DataOutput, R> parseResponse)
	{
		try
		{
			rawOut.writeByte(command.encode());
			rawOut.flush();
			sendParams.accept(rawIn, rawOut);
			return parseResponse.apply(rawIn, rawOut);
		} catch(IOException e)
		{
			throw new CommunicationException("Communication with the student side failed; maybe student called System.exit(0)", e);
		}
	}

	private void writeArgs(DataOutput out, List<String> params, List<IntRef> argRefs) throws IOException
	{
		int paramCount = params.size();
		if(paramCount != argRefs.size())
			throw new StudentCodeSeparatorException("Parameter and argument count mismatched: " + paramCount + ", " + argRefs.size());

		out.writeInt(paramCount);
		for(String param : params)
			out.writeUTF(param);
		for(IntRef argRef : argRefs)
			refManager.writeRef(out, argRef);
	}
}
