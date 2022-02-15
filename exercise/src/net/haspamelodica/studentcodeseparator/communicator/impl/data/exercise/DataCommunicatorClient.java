package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.CALL_CONSTRUCTOR;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.CALL_INSTANCE_METHOD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.CALL_STATIC_METHOD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.GET_CLASSNAME;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.GET_INSTANCE_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.GET_STATIC_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.RECEIVE;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.REF_DELETED;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.SEND;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.SET_INSTANCE_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.SET_STATIC_FIELD;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.Command.SHUTDOWN;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.Command;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.refs.IntRef;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.refs.IntRefManager;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.refs.IntRefManager.DeletedRef;
import net.haspamelodica.studentcodeseparator.exceptions.CommunicationException;
import net.haspamelodica.studentcodeseparator.exceptions.FrameworkCausedException;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DataCommunicatorClient<ATTACHMENT> implements StudentSideCommunicator<ATTACHMENT, IntRef<ATTACHMENT>>
{
	private final DataInputStream	rawIn;
	private final DataOutputStream	rawOut;

	private final Object communicationLock;

	private final IntRefManager<ATTACHMENT> refManager;

	private final AtomicBoolean	running;
	private final Thread		refCleanupThread;

	public DataCommunicatorClient(DataInputStream rawIn, DataOutputStream rawOut)
	{
		this.rawIn = rawIn;
		this.rawOut = rawOut;

		this.communicationLock = new Object();

		this.refManager = new IntRefManager<>();
		this.running = new AtomicBoolean(true);
		this.refCleanupThread = new Thread(this::refCleanupThread);
		refCleanupThread.start();
	}

	private void refCleanupThread()
	{
		while(running.get())
		{
			try
			{
				DeletedRef deletedRef = refManager.removeDeletedRef(r ->
				{
					synchronized(communicationLock)
					{
						r.run();
					}
				});
				refDeleted(deletedRef.id(), deletedRef.receivedCount());
			} catch(InterruptedException e)
			{
				// ignore: means the cleanup thread is being shut down
			} catch(CommunicationException e)
			{
				// ignore: means student side crashed; user-controlled threads will get this exception as well
			}
		}
	}

	public void shutdown()
	{
		running.set(false);
		refCleanupThread.interrupt();
		executeVoidCommand(SHUTDOWN, out ->
		{});
	}

	@Override
	public String getStudentSideClassname(IntRef<ATTACHMENT> ref)
	{
		return executeCommand(GET_CLASSNAME, out -> writeRef(out, ref), DataInput::readUTF);
	}

	@Override
	public <T> IntRef<ATTACHMENT> send(Serializer<T> serializer, IntRef<ATTACHMENT> serializerRef, T obj)
	{
		return executeRefCommand(SEND, out ->
		{
			writeRef(out, serializerRef);
			serializer.serialize(out, obj);
		});
	}

	@Override
	public <T> T receive(Serializer<T> serializer, IntRef<ATTACHMENT> serializerRef, IntRef<ATTACHMENT> objRef)
	{
		return executeCommand(RECEIVE, out ->
		{
			writeRef(out, serializerRef);
			writeRef(out, objRef);
		}, serializer::deserialize);
	}

	@Override
	public IntRef<ATTACHMENT> callConstructor(String cn, List<String> params, List<IntRef<ATTACHMENT>> argRefs)
	{
		return executeRefCommand(CALL_CONSTRUCTOR, out ->
		{
			out.writeUTF(cn);
			writeArgs(out, params, argRefs);
		});
	}

	@Override
	public IntRef<ATTACHMENT> callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<IntRef<ATTACHMENT>> argRefs)
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
	public IntRef<ATTACHMENT> getStaticField(String cn, String name, String fieldClassname)
	{
		return executeRefCommand(GET_STATIC_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
		});
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, IntRef<ATTACHMENT> valueRef)
	{
		executeVoidCommand(SET_STATIC_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			writeRef(out, valueRef);
		});
	}

	@Override
	public IntRef<ATTACHMENT> callInstanceMethod(String cn, String name, String returnClassname, List<String> params, IntRef<ATTACHMENT> receiverRef, List<IntRef<ATTACHMENT>> argRefs)
	{
		return executeRefCommand(CALL_INSTANCE_METHOD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(returnClassname);
			writeRef(out, receiverRef);
			writeArgs(out, params, argRefs);
		});
	}
	@Override
	public IntRef<ATTACHMENT> getInstanceField(String cn, String name, String fieldClassname, IntRef<ATTACHMENT> receiverRef)
	{
		return executeRefCommand(GET_INSTANCE_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			writeRef(out, receiverRef);
		});
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, IntRef<ATTACHMENT> receiverRef, IntRef<ATTACHMENT> valueRef)
	{
		executeVoidCommand(SET_INSTANCE_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			writeRef(out, receiverRef);
			writeRef(out, valueRef);
		});
	}

	private void refDeleted(int id, int receivedCount)
	{
		executeVoidCommand(REF_DELETED, out ->
		{
			// Can't use writeRef: the ref doesn't exist anymore
			out.writeInt(id);
			out.writeInt(receivedCount);
		});
	}

	private void executeVoidCommand(Command command, IOConsumer<DataOutput> sendParams)
	{
		executeCommand(command, sendParams, in -> null);
	}
	private IntRef<ATTACHMENT> executeRefCommand(Command command, IOConsumer<DataOutput> sendParams)
	{
		return executeCommand(command, sendParams, this::readRef);
	}
	private <R> R executeCommand(Command command, IOConsumer<DataOutput> sendParams, IOFunction<DataInput, R> parseResponse)
	{
		//TODO replace with more sophisticated synchronization approach allowing for multiple threads
		synchronized(communicationLock)
		{
			try
			{
				rawOut.writeByte(command.encode());
				sendParams.accept(rawOut);
				rawOut.flush();
				return parseResponse.apply(rawIn);
			} catch(IOException e)
			{
				throw new CommunicationException("Communication with the student side failed; maybe student called System.exit(0) or crashed", e);
			}
		}
	}

	private void writeArgs(DataOutput out, List<String> params, List<IntRef<ATTACHMENT>> argRefs) throws IOException
	{
		int paramCount = params.size();
		if(paramCount != argRefs.size())
			throw new FrameworkCausedException("Parameter and argument count mismatched: " + paramCount + ", " + argRefs.size());

		out.writeInt(paramCount);
		for(String param : params)
			out.writeUTF(param);
		for(IntRef<ATTACHMENT> argRef : argRefs)
			writeRef(out, argRef);
	}

	private IntRef<ATTACHMENT> readRef(DataInput in) throws IOException
	{
		return refManager.lookupReceivedRef(in.readInt());
	}

	private void writeRef(DataOutput out, IntRef<ATTACHMENT> ref) throws IOException
	{
		out.writeInt(refManager.getID(ref));
	}
}
