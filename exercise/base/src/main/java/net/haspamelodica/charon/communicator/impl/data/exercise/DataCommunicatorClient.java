package net.haspamelodica.charon.communicator.impl.data.exercise;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_STATIC_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_CALLBACK_INSTANCE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.DESCRIBE_TYPE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_ERROR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_ELEMENT;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_LENGTH;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_TYPE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_BY_NAME;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_HANDLED_BY_SERDES;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_OF;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.NEW_ARRAY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.NEW_ARRAY_WITH_INITIAL_VALUES;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.NEW_MULTI_ARRAY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.RECEIVE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SEND;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_ARRAY_ELEMENT;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.SET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.NEW_THREAD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.REF_DELETED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand.SHUTDOWN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.CALL_CALLBACK_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.GET_CALLBACK_INTERFACE_CN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.STUDENT_ERROR;
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
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
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
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
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
public class DataCommunicatorClient
		implements StudentSideCommunicator<LongRef, LongRef, ClientSideTransceiver<LongRef>, InternalCallbackManager<LongRef>>,
		ClientSideTransceiver<LongRef>,
		InternalCallbackManager<LongRef>
{
	private final StudentSideCommunicatorCallbacks<LongRef, LongRef> callbacks;

	private final DataStreamMultiplexer	multiplexer;
	private final AtomicInteger			nextInStreamID;
	private final AtomicInteger			nextOutStreamID;

	private final Queue<MultiplexedDataInputStream>		freeStreamsForReceiving;
	private final Queue<MultiplexedDataOutputStream>	freeStreamsForSending;

	private final Object commandStreamLock;

	private final ThreadLocal<StudentSideThread> threads;

	private final LongRefManager<LongRef> refManager;

	public DataCommunicatorClient(InputStream rawIn, OutputStream rawOut, StudentSideCommunicatorCallbacks<LongRef, LongRef> callbacks)
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
	public LongRef getTypeByName(String typeName)
	{
		return executeRefCommand(GET_TYPE_BY_NAME, DISALLOW_CALLBACKS, out -> out.writeUTF(typeName));
	}

	@Override
	public LongRef getArrayType(LongRef componentType)
	{
		return executeRefCommand(GET_ARRAY_TYPE, DISALLOW_CALLBACKS, out -> writeRef(out, componentType));
	}

	@Override
	public LongRef getTypeOf(LongRef ref)
	{
		return executeRefCommand(GET_TYPE_OF, DISALLOW_CALLBACKS, out -> writeRef(out, ref));
	}

	@Override
	public StudentSideTypeDescription<LongRef> describeType(LongRef type)
	{
		return executeCommand(DESCRIBE_TYPE, DISALLOW_CALLBACKS, out -> writeRef(out, type), in ->
		{
			// We can't neccessarily trust the kind given from the student side, but StudentSideType checks it.
			StudentSideTypeDescription.Kind kind = StudentSideTypeDescription.Kind.decode(in.readByte());

			String name = in.readUTF();

			Optional<LongRef> superclass = Optional.ofNullable(readRef(in));

			int superinterfacesCount = in.readInt();
			LongRef[] superinterfaces = new LongRef[superinterfacesCount];
			for(int i = 0; i < superinterfacesCount; i ++)
				superinterfaces[i] = readRef(in);

			Optional<LongRef> componentTypeIfArray = Optional.ofNullable(readRef(in));

			return new StudentSideTypeDescription<>(kind, name, superclass, List.of(superinterfaces), componentTypeIfArray);
		});
	}

	@Override
	public LongRef getTypeHandledBySerdes(LongRef serdesRef)
	{
		return executeRefCommand(GET_TYPE_HANDLED_BY_SERDES, ALLOW_CALLBACKS, out -> writeRef(out, serdesRef));
	}

	@Override
	public LongRef newArray(LongRef componentType, int length)
	{
		return executeRefCommand(NEW_ARRAY, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, componentType);
			out.writeInt(length);
		});
	}

	@Override
	public LongRef newMultiArray(LongRef componentType, List<Integer> dimensions)
	{
		return executeRefCommand(NEW_MULTI_ARRAY, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, componentType);

			int dimensionsSize = dimensions.size();
			out.writeInt(dimensionsSize);
			// index-based iteration to defend against botched List implementations
			for(int i = 0; i < dimensionsSize; i ++)
				out.writeInt(dimensions.get(i));
		});
	}

	@Override
	public LongRef newArrayWithInitialValues(LongRef componentType, List<LongRef> initialValues)
	{
		return executeRefCommand(NEW_ARRAY_WITH_INITIAL_VALUES, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, componentType);

			int length = initialValues.size();
			out.writeInt(length);
			// index-based iteration to defend against botched List implementations
			for(int i = 0; i < length; i ++)
				writeRef(out, initialValues.get(i));
		});
	}

	@Override
	public int getArrayLength(LongRef arrayRef)
	{
		return executeCommand(GET_ARRAY_LENGTH, DISALLOW_CALLBACKS, out -> writeRef(out, arrayRef), DataInput::readInt);
	}

	@Override
	public LongRef getArrayElement(LongRef arrayRef, int index)
	{
		return executeRefCommand(GET_ARRAY_ELEMENT, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayRef);
			out.writeInt(index);
		});
	}

	@Override
	public void setArrayElement(LongRef arrayRef, int index, LongRef valueRef)
	{
		executeVoidCommand(SET_ARRAY_ELEMENT, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayRef);
			out.writeInt(index);
			writeRef(out, valueRef);
		});
	}

	@Override
	public RefOrError<LongRef> callConstructor(LongRef type, List<LongRef> params, List<LongRef> argRefs)
	{
		return executeRefOrErrorCommand(CALL_CONSTRUCTOR, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			writeArgs(out, params, argRefs);
		});
	}

	@Override
	public RefOrError<LongRef> callStaticMethod(LongRef type, String name, LongRef returnType, List<LongRef> params, List<LongRef> argRefs)
	{
		return executeRefOrErrorCommand(CALL_STATIC_METHOD, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, returnType);
			writeArgs(out, params, argRefs);
		});
	}
	@Override
	public LongRef getStaticField(LongRef type, String name, LongRef fieldType)
	{
		return executeRefCommand(GET_STATIC_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, fieldType);
		});
	}
	@Override
	public void setStaticField(LongRef type, String name, LongRef fieldType, LongRef valueRef)
	{
		executeVoidCommand(SET_STATIC_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, fieldType);
			writeRef(out, valueRef);
		});
	}

	@Override
	public RefOrError<LongRef> callInstanceMethod(LongRef type, String name, LongRef returnType, List<LongRef> params,
			LongRef receiverRef, List<LongRef> argRefs)
	{
		return executeRefOrErrorCommand(CALL_INSTANCE_METHOD, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, returnType);
			writeArgs(out, params, argRefs);
			writeRef(out, receiverRef);
		});
	}
	@Override
	public LongRef getInstanceField(LongRef type, String name, LongRef fieldType, LongRef receiverRef)
	{
		return executeRefCommand(GET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, fieldType);
			writeRef(out, receiverRef);
		});
	}
	@Override
	public void setInstanceField(LongRef type, String name, LongRef fieldType, LongRef receiverRef, LongRef valueRef)
	{
		executeVoidCommand(SET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, fieldType);
			writeRef(out, receiverRef);
			writeRef(out, valueRef);
		});
	}

	@Override
	public ClientSideTransceiver<LongRef> getTransceiver()
	{
		return this;
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
		}, (in, serdesIn, error) ->
		{
			if(error)
				throw new StudentSideCausedException("Student side returned error in response to " + RECEIVE + ", which shouldn't be possible");
			T result = deserializer.deserialize(serdesIn);
			freeStreamsForReceiving.add(serdesIn);
			return result;
		});
	}

	@Override
	public InternalCallbackManager<LongRef> getCallbackManager()
	{
		return this;
	}

	@Override
	public LongRef createCallbackInstance(String interfaceCn)
	{
		LongRef callbackRef = refManager.createManagedRef();
		executeVoidCommand(CREATE_CALLBACK_INSTANCE, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, callbackRef);
			out.writeUTF(interfaceCn);
		});

		return callbackRef;
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
	private RefOrError<LongRef> executeRefOrErrorCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, allowedCallbacks, sendParams, this::readRefOrError);
	}
	private <R> R executeCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams, IOFunction<DataInput, R> parseResponse)
	{
		return executeCommand(command, allowedCallbacks, sendParams, (in, error) ->
		{
			if(error)
				throw new StudentSideCausedException("Student side returned error in response to " + command + ", which shouldn't be possible");
			return parseResponse.apply(in);
		});
	}
	private <R> R executeCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams, IOBiFunction<DataInput, Boolean, R> parseResponse)
	{
		return executeCommand(command, allowedCallbacks, out ->
		{
			sendParams.accept(out);
			return null;
		}, (in, params, error) -> parseResponse.apply(in, error));
	}
	private <R, P> R executeCommand(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOFunction<DataOutputStream, P> sendParams, IOTriFunction<DataInput, P, Boolean, R> parseResponse)
	{
		try
		{
			StudentSideThread thread = getStudentSideThread();
			MultiplexedDataInputStream in = thread.in();
			MultiplexedDataOutputStream out = thread.out();

			writeThreadCommand(out, command);
			P params = sendParams.apply(out);
			out.flush();

			boolean error = handleStudentSideResponsesUntilFinished(allowedCallbacks, in, out);
			return parseResponse.apply(in, params, error);
		} catch(UnexpectedResponseException e)
		{
			return wrapUnexpectedResponseException(e);
		} catch(IOException e)
		{
			return wrapIOException(e);
		}
	}

	private boolean handleStudentSideResponsesUntilFinished(AllowedThreadCallbacks allowedCallbacks, DataInput in, DataOutputStream out) throws IOException
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
					return false;
				}
				case STUDENT_ERROR ->
				{
					return true;
				}
				case CALL_CALLBACK_INSTANCE_METHOD ->
				{
					LongRef type = readRef(in);
					String name = in.readUTF();
					LongRef returnType = readRef(in);
					Args args = readArgs(in);
					LongRef receiverRef = readRef(in);

					//TODO maybe we want to abort callback if a CharonException is thrown?
					RefOrError<LongRef> result = callbacks.callCallbackInstanceMethod(type, name, returnType, args.params(),
							receiverRef, args.argRefs());

					writeThreadCommand(out, result.isError() ? EXERCISE_ERROR : EXERCISE_FINISHED);
					writeRef(out, result.resultOrErrorRef());
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
	private void writeArgs(DataOutput out, List<LongRef> params, List<LongRef> argRefs) throws IOException
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

	private RefOrError<LongRef> readRefOrError(DataInput in, boolean error) throws IOException
	{
		return new RefOrError<>(readRef(in), error);
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
		DISALLOW_CALLBACKS(STUDENT_FINISHED, STUDENT_ERROR),
		ALLOW_CALLBACKS(STUDENT_FINISHED, STUDENT_ERROR, GET_CALLBACK_INTERFACE_CN, CALL_CALLBACK_INSTANCE_METHOD);

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
