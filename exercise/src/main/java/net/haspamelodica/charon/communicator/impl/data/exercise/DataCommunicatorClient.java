package net.haspamelodica.charon.communicator.impl.data.exercise;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_STATIC_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_CALLBACK_INSTANCE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_CLASSNAME;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_INTERFACES;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_SUPERCLASS;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.RECEIVE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SEND;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.NEW_THREAD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.REF_DELETED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.SHUTDOWN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.CALL_CALLBACK_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.GET_CALLBACK_INTERFACE_CN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.STUDENT_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient.AllowedThreadCallbacks.ALLOW_CALLBACKS;
import static net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient.AllowedThreadCallbacks.DISALLOW_CALLBACKS;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorConstants;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils.Args;
import net.haspamelodica.charon.communicator.impl.data.ThreadCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentResponse;
import net.haspamelodica.charon.communicator.impl.data.ThreadResponse;
import net.haspamelodica.charon.exceptions.CommunicationException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.IllegalBehaviourException;
import net.haspamelodica.charon.marshaling.Deserializer;
import net.haspamelodica.charon.marshaling.Serializer;
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
	private final StudentSideCommunicatorCallbacks<LongRef> callbacks;

	private final DataStreamMultiplexer	multiplexer;
	private final AtomicInteger			nextInStreamID;
	private final AtomicInteger			nextOutStreamID;

	private final Queue<MultiplexedDataInputStream>		freeStreamsForReceiving;
	private final Queue<MultiplexedDataOutputStream>	freeStreamsForSending;

	private final Object commandStreamLock;

	private final ThreadLocal<StudentSideThread> threads;

	private final LongRefManager<LongRef> refManager;

	public DataCommunicatorClient(InputStream rawIn, OutputStream rawOut, StudentSideCommunicatorCallbacks<LongRef> callbacks)
	{
		this.callbacks = callbacks;
		this.multiplexer = new BufferedDataStreamMultiplexer(rawIn, rawOut);
		this.nextInStreamID = new AtomicInteger(DataCommunicatorConstants.FIRST_FREE_STREAM_ID);
		this.nextOutStreamID = new AtomicInteger(DataCommunicatorConstants.FIRST_FREE_STREAM_ID);

		this.freeStreamsForReceiving = new ConcurrentLinkedQueue<>();
		this.freeStreamsForSending = new ConcurrentLinkedQueue<>();

		this.threads = new ThreadLocal<>();

		this.commandStreamLock = new Object();

		this.refManager = new SimpleLongRefManager(true);
	}

	public void shutdown()
	{
		executeThreadIndependentCommand(SHUTDOWN, commandOut ->
		{}, commandIn ->
		{
			if(ThreadIndependentResponse.decode(commandIn.readByte()) != ThreadIndependentResponse.SHUTDOWN_FINISHED)
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
		return executeCommand(GET_CLASSNAME, DISALLOW_CALLBACKS, out -> writeRef(out, ref), DataInput::readUTF);
	}
	@Override
	public String getSuperclass(String cn)
	{
		return executeCommand(GET_SUPERCLASS, DISALLOW_CALLBACKS, out -> out.writeUTF(cn), DataInput::readUTF);
	}
	@Override
	public List<String> getInterfaces(String cn)
	{
		return executeCommand(GET_INTERFACES, DISALLOW_CALLBACKS, out -> out.writeUTF(cn), in ->
		{
			int n = in.readInt();
			String[] result = new String[n];
			for(int i = 0; i < n; i ++)
				result[i] = in.readUTF();
			return List.of(result);
		});
	}

	@Override
	public <T> LongRef send(LongRef serdesRef, Serializer<T> serializer, T obj)
	{
		return executeRefCommand(SEND, ALLOW_CALLBACKS, out ->
		{
			MultiplexedDataOutputStream serdesOut = freeStreamsForSending.poll();
			if(serdesOut == null)
				serdesOut = nextOutStream();

			writeRef(out, serdesRef);
			out.writeInt(serdesOut.getStreamID());
			out.flush();
			serializer.serialize(serdesOut, obj);
			serdesOut.flush();

			freeStreamsForSending.add(serdesOut);
		});
	}
	@Override
	public <T> T receive(LongRef serdesRef, Deserializer<T> deserializer, LongRef objRef)
	{
		return executeCommand(RECEIVE, ALLOW_CALLBACKS, out ->
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
			T result = deserializer.deserialize(serdesIn);
			freeStreamsForReceiving.add(serdesIn);
			return result;
		});
	}

	@Override
	public LongRef callConstructor(String cn, List<String> params, List<LongRef> argRefs)
	{
		return executeRefCommand(CALL_CONSTRUCTOR, ALLOW_CALLBACKS, out ->
		{
			out.writeUTF(cn);
			writeArgs(out, params, argRefs);
		});
	}

	@Override
	public LongRef callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<LongRef> argRefs)
	{
		return executeRefCommand(CALL_STATIC_METHOD, ALLOW_CALLBACKS, out ->
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
		return executeRefCommand(GET_STATIC_FIELD, DISALLOW_CALLBACKS, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
		});
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, LongRef valueRef)
	{
		executeVoidCommand(SET_STATIC_FIELD, DISALLOW_CALLBACKS, out ->
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
		return executeRefCommand(CALL_INSTANCE_METHOD, ALLOW_CALLBACKS, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(returnClassname);
			writeArgs(out, params, argRefs);
			writeRef(out, receiverRef);
		});
	}
	@Override
	public LongRef getInstanceField(String cn, String name, String fieldClassname, LongRef receiverRef)
	{
		return executeRefCommand(GET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
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
		executeVoidCommand(SET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
		{
			out.writeUTF(cn);
			out.writeUTF(name);
			out.writeUTF(fieldClassname);
			writeRef(out, receiverRef);
			writeRef(out, valueRef);
		});
	}

	@Override
	public LongRef createCallbackInstance(String interfaceCn)
	{
		//TODO callbacks need a Ref managed by the exercise side
		return executeRefCommand(CREATE_CALLBACK_INSTANCE, DISALLOW_CALLBACKS, out -> out.writeUTF(interfaceCn));
	}

	private void refDeleted(int id, int receivedCount)
	{
		executeThreadIndependentCommand(REF_DELETED, commandOut ->
		{
			// Can't use writeRef: the ref doesn't exist anymore
			commandOut.writeInt(id);
			commandOut.writeInt(receivedCount);
		});
	}

	private void executeVoidCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks, IOConsumer<DataOutputStream> sendParams)
	{
		executeCommand(command, allowedCallbacks, sendParams, in -> null);
	}
	private LongRef executeRefCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks, IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, allowedCallbacks, sendParams, this::readRef);
	}
	private <R> R executeCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams, IOFunction<DataInput, R> parseResponse)
	{
		return executeCommand(command, allowedCallbacks, out ->
		{
			sendParams.accept(out);
			return null;
		}, (in, params) -> parseResponse.apply(in));
	}
	private <R, P> R executeCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOFunction<DataOutputStream, P> sendParams, IOBiFunction<DataInput, P, R> parseResponse)
	{
		try
		{
			StudentSideThread thread = getStudentSideThread();
			MultiplexedDataInputStream in = thread.in();
			MultiplexedDataOutputStream out = thread.out();

			writeThreadCommand(out, command);
			P params = sendParams.apply(out);
			out.flush();

			handleStudentSideResponsesUntilFinished(allowedCallbacks, in, out);
			return parseResponse.apply(in, params);
		} catch(UnexpectedResponseException e)
		{
			return wrapUnexpectedResponseException(e);
		} catch(IOException e)
		{
			return wrapIOException(e);
		}
	}

	private void handleStudentSideResponsesUntilFinished(AllowedThreadCallbacks allowedCallbacks, DataInput in, DataOutputStream out) throws IOException
	{
		for(;;)
		{
			ThreadResponse response = readThreadResponse(in);
			if(!allowedCallbacks.allows(response))
				throw new IllegalBehaviourException("Illegal response from student side: " + response);
			switch(response)
			{
				case STUDENT_FINISHED ->
				{
					return;
				}
				case CALL_CALLBACK_INSTANCE_METHOD ->
				{
					String cn = in.readUTF();
					String name = in.readUTF();
					String returnClassname = in.readUTF();
					Args args = readArgs(in);
					LongRef receiverRef = readRef(in);

					LongRef result = callbacks.callCallbackInstanceMethod(cn, name, returnClassname, args.params(), receiverRef, args.argRefs());

					writeThreadCommand(out, EXERCISE_FINISHED);
					writeRef(out, result);
				}
				case GET_CALLBACK_INTERFACE_CN ->
				{
					LongRef callbackRef = readRef(in);

					String result = callbacks.getCallbackInterfaceCn(callbackRef);

					writeThreadCommand(out, EXERCISE_FINISHED);
					out.writeUTF(result);
				}
			}
			out.flush();
		}
	}

	private void executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutput> sendCommand)
	{
		executeThreadIndependentCommand(command, sendCommand, in -> null);
	}
	private <R> R executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutput> sendCommand,
			IOFunction<DataInput, R> parseResponse)
	{
		synchronized(commandStreamLock)
		{
			try
			{
				MultiplexedDataOutputStream commandOut = multiplexer.getOut(DataCommunicatorConstants.THREAD_INDEPENDENT_COMMAND_STERAM_ID);
				MultiplexedDataInputStream commandIn = multiplexer.getIn(DataCommunicatorConstants.THREAD_INDEPENDENT_COMMAND_STERAM_ID);
				commandOut.writeByte(command.encode());
				sendCommand.accept(commandOut);
				//TODO this spuriously throws "another thread is currently writing"
				commandOut.flush();
				return parseResponse.apply(commandIn);
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

	//TODO clean up threads which have exited
	private StudentSideThread createStudentSideThread() throws ClosedException
	{
		// This thread's in stream is only usable as soon as the student side created the corresponding output stream.
		// But the first communication between two threads is always a write by the exercise side,
		// which will (according to GenericStreamMultiplexer) finish only when read is called on the corresponding input stream.
		// The student side does this only after creating the output stream, which means this won't cause problems.
		// (If the student side is malicious and doesn't create the output stream, all it can do is cause UnexpectedResponseExceptions.)
		MultiplexedDataInputStream in = nextInStream();
		MultiplexedDataOutputStream out = nextOutStream();
		executeThreadIndependentCommand(NEW_THREAD, commandOut ->
		{
			commandOut.writeInt(in.getStreamID());
			commandOut.writeInt(out.getStreamID());
		});
		return new StudentSideThread(in, out);
	}

	private Args readArgs(DataInput in) throws IOException
	{
		return DataCommunicatorUtils.readArgs(in, this::readRef);
	}
	private void writeArgs(DataOutput out, List<String> params, List<LongRef> argRefs) throws IOException
	{
		DataCommunicatorUtils.writeArgs(out, params, argRefs, FrameworkCausedException::new, this::writeRef);
	}

	private ThreadResponse readThreadResponse(DataInput in) throws IOException
	{
		byte encodedResponse = in.readByte();
		ThreadResponse result = ThreadResponse.decode(encodedResponse);
		if(result == null)
			throw new IllegalBehaviourException("Illegal thread response: " + encodedResponse);
		return result;
	}
	private void writeThreadCommand(DataOutput out, ThreadCommand command) throws IOException
	{
		out.writeByte(command.encode());
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

	protected static enum AllowedThreadCallbacks
	{
		DISALLOW_CALLBACKS(STUDENT_FINISHED),
		ALLOW_CALLBACKS(STUDENT_FINISHED, GET_CALLBACK_INTERFACE_CN, CALL_CALLBACK_INSTANCE_METHOD);

		private final Set<ThreadResponse> allowedResponses;

		private AllowedThreadCallbacks(ThreadResponse... allowedResponses)
		{
			this.allowedResponses = Set.of(allowedResponses);
		}

		public boolean allows(ThreadResponse response)
		{
			return allowedResponses.contains(response);
		}
	}
}
