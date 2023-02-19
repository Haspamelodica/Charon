package net.haspamelodica.charon.communicator.impl.data.exercise;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_STATIC_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_CALLBACK_INSTANCE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_CLASSNAME;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.RECEIVE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SEND;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.NEW_THREAD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.REF_DELETED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.SHUTDOWN;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.ThreadCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentResponse;
import net.haspamelodica.charon.communicator.impl.data.ThreadResponse;
import net.haspamelodica.charon.exceptions.CommunicationException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.IllegalBehaviourException;
import net.haspamelodica.charon.refs.longref.LongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.streammultiplexer.BufferedDataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.ClosedException;
import net.haspamelodica.streammultiplexer.DataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.MultiplexedDataInputStream;
import net.haspamelodica.streammultiplexer.MultiplexedDataOutputStream;
import net.haspamelodica.streammultiplexer.UnexpectedResponseException;

// TODO server, client or both crash on shutdown sometimes
public class DataCommunicatorClient implements StudentSideCommunicatorClientSide<LongRef>
{
	private final DataStreamMultiplexer	multiplexer;
	private final AtomicInteger			nextInStreamID;
	private final AtomicInteger			nextOutStreamID;

	private final Queue<MultiplexedDataInputStream>		freeStreamsForReceiving;
	private final Queue<MultiplexedDataOutputStream>	freeStreamsForSending;

	private final Object out0Lock;

	private final ThreadLocal<StudentSideThread> threads;

	private final LongRefManager<LongRef> refManager;

	public DataCommunicatorClient(InputStream rawIn, OutputStream rawOut)
	{
		this.multiplexer = new BufferedDataStreamMultiplexer(rawIn, rawOut);
		this.nextInStreamID = new AtomicInteger(1);
		this.nextOutStreamID = new AtomicInteger(1);

		this.freeStreamsForReceiving = new ConcurrentLinkedQueue<>();
		this.freeStreamsForSending = new ConcurrentLinkedQueue<>();

		this.threads = new ThreadLocal<>();

		this.out0Lock = new Object();

		this.refManager = new SimpleLongRefManager(false);
	}

	public void shutdown()
	{
		executeThreadIndependentCommand(SHUTDOWN, out0 ->
		{}, in0 ->
		{
			if(ThreadIndependentResponse.decode(in0.readByte()) != ThreadIndependentResponse.SHUTDOWN_FINISHED)
				throw new IllegalBehaviourException("Student side didn't respond with SHUTDOWN_FINISHED to SHUTDOWN");
			return null;
		});
		multiplexer.close();
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return false;
	}

	@Override
	public String getClassname(LongRef ref)
	{
		return executeCommand(GET_CLASSNAME, out -> writeRef(out, ref), DataInput::readUTF);
	}

	@Override
	public <T> LongRef send(LongRef serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		return executeRefCommand(SEND, out ->
		{
			MultiplexedDataOutputStream serdesOut = freeStreamsForSending.poll();
			if(serdesOut == null)
				serdesOut = nextOutStream();

			writeRef(out, serdesRef);
			out.writeInt(serdesOut.getStreamID());
			out.flush();
			sendObj.accept(serdesOut, obj);
			serdesOut.flush();

			freeStreamsForSending.add(serdesOut);
		});
	}
	@Override
	public <T> T receive(LongRef serdesRef, IOFunction<DataInput, T> receiveObj, LongRef objRef)
	{
		return executeCommand(RECEIVE, out ->
		{
			MultiplexedDataInputStream serdesIn = freeStreamsForReceiving.poll();
			if(serdesIn == null)
				serdesIn = nextInStream();

			writeRef(out, serdesRef);
			writeRef(out, objRef);
			out.writeInt(serdesIn.getStreamID());

			return serdesIn;
		}, (in, serdesIn) ->
		{
			// The student side notifies us it is finished with createing the output stream behind serdesIn by sending SERDES_OUT_READY.
			// Neccessary because StreamMultiplexer requires the output stream to exist before the input stream is used.
			if(ThreadResponse.decode(in.readByte()) != ThreadResponse.SERDES_OUT_READY)
				throw new IllegalBehaviourException("Expected " + ThreadResponse.SERDES_OUT_READY);
			T result = receiveObj.apply(serdesIn);
			freeStreamsForReceiving.add(serdesIn);
			return result;
		});
	}

	@Override
	public LongRef callConstructor(String cn, List<String> params, List<LongRef> argRefs)
	{
		return executeRefCommand(CALL_CONSTRUCTOR, out ->
		{
			out.writeUTF(cn);
			writeArgs(out, params, argRefs);
		});
	}

	@Override
	public LongRef callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<LongRef> argRefs)
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
	public LongRef getStaticField(String cn, String name, String fieldClassname)
	{
		return executeRefCommand(GET_STATIC_FIELD, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
		});
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, LongRef valueRef)
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
	public LongRef callInstanceMethod(String cn, String name, String returnClassname, List<String> params, LongRef receiverRef, List<LongRef> argRefs)
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
	public LongRef getInstanceField(String cn, String name, String fieldClassname, LongRef receiverRef)
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
	public void setInstanceField(String cn, String name, String fieldClassname, LongRef receiverRef, LongRef valueRef)
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

	@Override
	public LongRef createCallbackInstance(String interfaceName, Callback<LongRef> callback)
	{
		//TODO do something with the passed callback
		//TODO cache callbacks so that == works
		return executeRefCommand(CREATE_CALLBACK_INSTANCE, out ->
		{
			out.writeUTF(interfaceName);
		});
	}

	private void refDeleted(int id, int receivedCount)
	{
		executeThreadIndependentCommand(REF_DELETED, out0 ->
		{
			// Can't use writeRef: the ref doesn't exist anymore
			out0.writeInt(id);
			out0.writeInt(receivedCount);
		});
	}

	private void executeVoidCommand(ThreadCommand command, IOConsumer<DataOutputStream> sendParams)
	{
		executeCommand(command, sendParams, in -> null);
	}
	private LongRef executeRefCommand(ThreadCommand command, IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, sendParams, this::readRef);
	}
	private <R> R executeCommand(ThreadCommand command, IOConsumer<DataOutputStream> sendParams, IOFunction<DataInput, R> parseResponse)
	{
		return executeCommand(command, out ->
		{
			sendParams.accept(out);
			return null;
		}, (in, params) -> parseResponse.apply(in));
	}
	private <R, P> R executeCommand(ThreadCommand command, IOFunction<DataOutputStream, P> sendParams, IOBiFunction<DataInput, P, R> parseResponse)
	{
		try
		{
			StudentSideThread thread = getStudentSideThread();

			MultiplexedDataOutputStream out = thread.out();
			out.writeByte(command.encode());
			P params = sendParams.apply(out);
			out.flush();

			return parseResponse.apply(thread.in(), params);
		} catch(UnexpectedResponseException e)
		{
			return wrapUnexpectedResponseException(e);
		} catch(IOException e)
		{
			return wrapIOException(e);
		}
	}

	private void executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutput> sendCommand)
	{
		executeThreadIndependentCommand(command, sendCommand, in -> null);
	}
	private <R> R executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutput> sendCommand,
			IOFunction<DataInput, R> parseResponse)
	{
		synchronized(out0Lock)
		{
			try
			{
				MultiplexedDataOutputStream out0 = multiplexer.getOut(0);
				MultiplexedDataInputStream in0 = multiplexer.getIn(0);
				out0.writeByte(command.encode());
				sendCommand.accept(out0);
				//TODO this spuriously throws "another thread is currently writing"
				out0.flush();
				return parseResponse.apply(in0);
			} catch(UnexpectedResponseException e)
			{
				return wrapUnexpectedResponseException(e);
			} catch(IOException e)
			{
				return wrapIOException(e);
			}
		}
	}

	private <R> R wrapUnexpectedResponseException(UnexpectedResponseException e)
	{
		throw new IllegalBehaviourException(e);
	}
	private <R> R wrapIOException(IOException e)
	{
		throw new CommunicationException("Communication with the student side failed; maybe student called System.exit(0) or crashed", e);
	}

	private StudentSideThread getStudentSideThread() throws ClosedException
	{
		StudentSideThread thread = threads.get();
		// Can't use a supplied ThreadLocal because of the ClosedException
		if(thread == null)
		{
			thread = createStudentSideThread();
			threads.set(thread);
		}
		return thread;
	}

	private StudentSideThread createStudentSideThread() throws ClosedException
	{
		// in only is usable as soon as the student side created the corresponding output stream.
		// But the first communication between two threads is always a write by the exercise side,
		// which will (according to GenericStreamMultiplexer) finish only when read is called on the corresponding input stream.
		// The student side does this only after creating the output stream, which means this won't cause problems.
		// (If the student side is malicious and doesn't create the output stream, all it can do is cause UnexpectedResponseExceptions.)
		MultiplexedDataInputStream in = nextInStream();
		MultiplexedDataOutputStream out = nextOutStream();
		executeThreadIndependentCommand(NEW_THREAD, out0 ->
		{
			out0.writeInt(in.getStreamID());
			out0.writeInt(out.getStreamID());
		});
		return new StudentSideThread(in, out);
	}

	private void writeArgs(DataOutput out, List<String> params, List<LongRef> argRefs) throws IOException
	{
		int paramCount = params.size();
		if(paramCount != argRefs.size())
			throw new FrameworkCausedException("Parameter and argument count mismatched: " + paramCount + ", " + argRefs.size());

		out.writeInt(paramCount);
		for(String param : params)
			out.writeUTF(param);
		for(LongRef argRef : argRefs)
			writeRef(out, argRef);
	}

	private LongRef readRef(DataInput in) throws IOException
	{
		return refManager.unmarshalReceivedId(in.readLong());
	}
	private void writeRef(DataOutput out, LongRef ref) throws IOException
	{
		out.writeLong(refManager.marshalRefForSending(ref));
	}

	private MultiplexedDataInputStream nextInStream() throws ClosedException
	{
		return multiplexer.getIn(nextInStreamID.getAndIncrement());
	}

	private MultiplexedDataOutputStream nextOutStream() throws ClosedException
	{
		return multiplexer.getOut(nextOutStreamID.getAndIncrement());
	}

	private static record StudentSideThread(MultiplexedDataInputStream in, MultiplexedDataOutputStream out)
	{}
}
