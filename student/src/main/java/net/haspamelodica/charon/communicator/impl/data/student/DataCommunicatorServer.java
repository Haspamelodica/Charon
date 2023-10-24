package net.haspamelodica.charon.communicator.impl.data.student;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentRequest.CRASHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentRequest.OUT_OF_MEMORY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentRequest.SHUTDOWN_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.CALL_CALLBACK_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.GET_CALLBACK_INTERFACE_CN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.STUDENT_FINISHED;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegativeInMultiArray;
import net.haspamelodica.charon.OperationOutcome.Kind;
import net.haspamelodica.charon.communicator.ExternalCallbackManager;
import net.haspamelodica.charon.communicator.ServerSideTransceiver;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils;
import net.haspamelodica.charon.communicator.impl.data.ThreadCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadResponse;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.refs.longref.LongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;
import net.haspamelodica.exchanges.DataExchange;
import net.haspamelodica.exchanges.Exchange;
import net.haspamelodica.exchanges.ExchangePool;
import net.haspamelodica.exchanges.util.ClosedException;
import net.haspamelodica.exchanges.util.IOBiConsumer;

public class DataCommunicatorServer
{
	//TODO make these configurable
	private static final int	OOME_WAIT_PERIOD_MS		= 100;
	private static final int	OOME_EMERGENCY_BYTES	= 10_000_000;

	private final StudentSideCommunicator<LongRef, LongRef, LongRef, LongRef, LongRef, LongRef, ? extends ServerSideTransceiver<LongRef>,
			? extends ExternalCallbackManager<LongRef>> communicator;

	private final ExchangePool							exchangePool;
	private final LongRefManager<LongRef>				refManager;
	private final UnidirectionalMap<LongRef, Integer>	constructorParamCounts;
	private final UnidirectionalMap<LongRef, Integer>	methodParamCounts;
	private final ThreadLocal<ExerciseSideThread>		threads;
	private final AtomicBoolean							running;

	// The value of this field is never used, but the field is still needed: It exists to be able to
	// reliably free up some memory in case of an OOME, to be able to notify the exercise side.
	@SuppressWarnings("unused")
	private byte[]			oomeEmergencyMemory;
	private final byte[]	oomeEmergencyLength1ByteArray;

	private final DataExchange threadIndependentExchange;

	public DataCommunicatorServer(ExchangePool exchangePool, RefTranslatorCommunicatorSupplier<LongRef,
			ServerSideTransceiver<LongRef>, ExternalCallbackManager<LongRef>,
			RefTranslatorCommunicatorCallbacks<LongRef>> communicatorSupplier) throws IOException
	{
		this.exchangePool = exchangePool;
		this.refManager = new SimpleLongRefManager(false);
		this.communicator = communicatorSupplier.createCommunicator(false,
				new StudentSideCommunicatorCallbacks<>()
				{
					@Override
					public String getCallbackInterfaceCn(LongRef callbackRef)
					{
						try
						{
							return DataCommunicatorServer.this.getCallbackInterfaceCn(callbackRef);
						} catch(IOException e)
						{
							// If there's an IOException while communicating with the exercise side, nothing matters anymore.
							throw new UncheckedIOException(e);
						}
					}

					@Override
					public CallbackOperationOutcome<LongRef, LongRef> callCallbackInstanceMethod(LongRef type, String name, LongRef returnType, List<LongRef> params,
							LongRef receiverRef, List<LongRef> argRefs)
					{
						try
						{
							return DataCommunicatorServer.this.callCallbackInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
						} catch(IOException e)
						{
							// If there's an IOException while communicating with the exercise side, nothing matters anymore.
							throw new UncheckedIOException(e);
						}
					}
				},
				new RefTranslatorCommunicatorCallbacks<>()
				{
					@Override
					public <REF_FROM, TYPEREF_FROM extends REF_FROM> LongRef createForwardRef(UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef)
					{
						return refManager.createManagedRef();
					}
				});
		this.constructorParamCounts = UnidirectionalMap.builder().concurrent().identityMap().weakKeys().build();
		this.methodParamCounts = UnidirectionalMap.builder().concurrent().identityMap().weakKeys().build();
		this.threads = new ThreadLocal<>();
		this.running = new AtomicBoolean();

		this.oomeEmergencyMemory = new byte[OOME_EMERGENCY_BYTES];
		this.oomeEmergencyLength1ByteArray = new byte[1];

		// unbuffered because we need this for OOMEs as well,
		// where we want as few intermediaries as possible.
		this.threadIndependentExchange = createDataExchange(false);
	}

	// Don't even try to catch IOExceptions; just crash.
	// Exercise has to handle this correctly anyway as this behaviour could also created maliciously.
	public void run() throws IOException
	{
		running.set(true);
		DataInputStream commandIn = threadIndependentExchange.in();
		try
		{
			loop: for(;;)
			{
				switch(ThreadIndependentCommand.decode(commandIn.readByte()))
				{
					case NEW_THREAD -> respondNewThread(commandIn);
					case REF_DELETED -> respondRefDeleted(commandIn);
					case SHUTDOWN ->
					{
						break loop;
					}
				}
			}
			running.set(false);
			// notify exercise side we received the shutdown signal
			threadIndependentExchange.out().writeByte(SHUTDOWN_FINISHED.encode());
			threadIndependentExchange.out().flush();
			exchangePool.close();
		} catch(RuntimeException e)
		{
			//TODO log to somewhere instead of rethrowing
			throw e;
		}
	}

	private void outOfMemory(OutOfMemoryError cause)
	{
		try
		{
			// Don't wrap in synchronized; it's not a big problem if this happens while the student side is shutting down,
			// and a synchronized block may cause some allocations.
			// Also, this entire method can't be reliable either way; if an OOME has occurred, we can't be sure of anything.
			if(!running.compareAndSet(false, true))
				return;

			// In most cases when an OOME occurs, one thread is allocating the bulk of all objects.
			// However, it's possible that another concurrent thread
			// tries to allocate an object at an "unlucky" time and gets the OOME instead.
			// In that case, sleeping for a bit helps because that way
			// we can hopefully force the real culprit to crash as well.
			try
			{
				Thread.sleep(OOME_WAIT_PERIOD_MS);
			} catch(InterruptedException e)
			{
				// Ignore. We want to notify the exercise side even if our waiting period was cut short.
			}
		} catch(OutOfMemoryError e)
		{
			// Ignore all additional OOMEs while doing heuristics to be as reliable as possible
			// in notifying the exercise side.
			// And yes, Thread.sleep can throws OOMEs.
		}

		// Release this object to free up some space, in case the Exchange needs some memory in write(),
		// but only release after the waiting period is done to avoid this memory getting eaten away by other threads.
		oomeEmergencyMemory = null;
		oomeEmergencyLength1ByteArray[0] = OUT_OF_MEMORY.encode();
		try
		{
			// Not using writeByte here is not pretty, but that method has a higher chance of causing allocations.
			threadIndependentExchange.out().write(oomeEmergencyLength1ByteArray);
			threadIndependentExchange.out().flush();

			// Try to shut down. Won't be reliable either way, and as always,
			// the exercise side needs to be handle malicious students either way,
			// but it makes receiving the OOME more reliable for the exercise side.
			// DataCommunicatorClient does call close() on its exchangePool on receiving OOME,
			// but if that doesn't cause all outstanding reads to fail,
			// it'll help if the student side closes its exchange pool as well.
			// However, wait a bit before doing that to make sure the client's
			// threadIndependentExchange is done setting studentSideCrashReason
			// before the individual thread's exchanges start to fail.
			try
			{
				Thread.sleep(1000);
			} catch(InterruptedException e)
			{
				// ignore
			}
			exchangePool.close();
		} catch(IOException e)
		{
			// If there's an IOException while communicating with the exercise side, nothing matters anymore.
			// Also, this method isn't reliable either way.
		}
	}

	private void crashed(Throwable cause)
	{
		if(!running.get())
			return;

		try
		{
			threadIndependentExchange.out().writeByte(CRASHED.encode());
			threadIndependentExchange.out().writeUTF(cause.toString());
			threadIndependentExchange.out().flush();

			// Try to shut down.
			// This makes receiving the crash more reliable for the exercise side:
			// DataCommunicatorClient does call close() on its exchangePool on receiving CRASHED,
			// but if that doesn't cause all outstanding reads to fail,
			// it'll help if the student side closes its exchange pool as well.
			// However, wait a bit before doing that to make sure the client's
			// threadIndependentExchange is done setting studentSideCrashReason
			// before the individual thread's exchanges start to fail.
			try
			{
				Thread.sleep(1000);
			} catch(InterruptedException e)
			{
				// ignore
			}
			exchangePool.close();
		} catch(IOException e)
		{
			// If there's an IOException while communicating with the exercise side, nothing matters anymore.
			throw new UncheckedIOException(e);
		}
	}

	private void handleExerciseSideCommandsUntilFinished(DataExchange control)
	{
		DataInputStream in = control.in();
		DataOutputStream out = control.out();
		try
		{
			for(;;)
			{
				switch(readThreadCommand(in))
				{
					case EXERCISE_FINISHED ->
					{
						return;
					}
					case GET_TYPE_BY_NAME -> respondGetTypeByName(in, out);
					case GET_ARRAY_TYPE -> respondGetArrayType(in, out);
					case GET_TYPE_OF -> respondGetTypeOf(in, out);
					case DESCRIBE_TYPE -> respondDescribeType(in, out);
					case GET_TYPE_HANDLED_BY_SERDES -> respondGetTypeHandledBySerdes(in, out);
					case CREATE_ARRAY -> respondNewArray(in, out);
					case CREATE_MULTI_ARRAY -> respondNewMultiArray(in, out);
					case INITIALIZE_ARRAY -> respondNewArrayWithInitialValues(in, out);
					case GET_ARRAY_LENGTH -> respondGetArrayLength(in, out);
					case GET_ARRAY_ELEMENT -> respondGetArrayElement(in, out);
					case SET_ARRAY_ELEMENT -> respondSetArrayElement(in, out);
					case LOOKUP_CONSTRUCTOR -> respondLookupConstructor(in, out);
					case LOOKUP_STATIC_METHOD -> respondLookupMethod(in, out, true);
					case LOOKUP_STATIC_FIELD -> respondLookupField(in, out, true);
					case LOOKUP_INSTANCE_METHOD -> respondLookupMethod(in, out, false);
					case LOOKUP_INSTANCE_FIELD -> respondLookupField(in, out, false);
					case CALL_CONSTRUCTOR -> respondCallConstructor(in, out);
					case CALL_STATIC_METHOD -> respondCallStaticMethod(in, out);
					case GET_STATIC_FIELD -> respondGetStaticField(in, out);
					case SET_STATIC_FIELD -> respondSetStaticField(in, out);
					case CALL_INSTANCE_METHOD -> respondCallInstanceMethod(in, out);
					case GET_INSTANCE_FIELD -> respondGetInstanceField(in, out);
					case SET_INSTANCE_FIELD -> respondSetInstanceField(in, out);
					case SEND -> respondSend(in, out);
					case RECEIVE -> respondReceive(in, out);
					case CREATE_CALLBACK_INSTANCE -> respondCreateCallbackInstance(in, out);
				}
				out.flush();
			}
		} catch(RuntimeException e)
		{
			if(!running.get())
				// ignore; we are shutting down
				return;

			//TODO log to somewhere instead of rethrowing
			throw e;
		} catch(IOException e)
		{
			if(!running.get())
				// ignore; we are shutting down
				return;

			//TODO do we need to do something with this exception?
			// I don't think so because once the exchange pool or one of its streams throws any exception,
			// it will continue to throw that exception forever
			throw new UncheckedIOException(e);
		}
	}

	private void respondGetTypeByName(DataInput in, DataOutput out) throws IOException
	{
		String typeName = in.readUTF();

		OperationOutcome<LongRef, Void, LongRef> result = communicator.getTypeByName(typeName);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondGetArrayType(DataInput in, DataOutput out) throws IOException
	{
		LongRef componentType = readRef(in);

		LongRef result = communicator.getArrayType(componentType);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondGetTypeOf(DataInput in, DataOutput out) throws IOException
	{
		LongRef ref = readRef(in);

		LongRef result = communicator.getTypeOf(ref);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondDescribeType(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		StudentSideTypeDescription<LongRef> result = communicator.describeType(type);
		writeThreadResponse(out, STUDENT_FINISHED);
		out.writeByte(result.kind().encode());
		out.writeUTF(result.name());
		writeRef(out, result.superclass().orElse(null));
		writeRefs(out, result.superinterfaces());

		writeRef(out, result.componentTypeIfArray().orElse(null));
	}

	private void respondGetTypeHandledBySerdes(DataInput in, DataOutput out) throws IOException
	{
		LongRef serdesRef = readRef(in);

		LongRef result = communicator.getTypeHandledBySerdes(serdesRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondNewArray(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayType = readRef(in);
		int length = in.readInt();

		OperationOutcome<LongRef, Void, LongRef> result = communicator.createArray(arrayType, length);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondNewMultiArray(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayType = readRef(in);

		List<Integer> dimensions = DataCommunicatorUtils.readList(in, DataInput::readInt);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.createMultiArray(arrayType, dimensions);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondNewArrayWithInitialValues(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayType = readRef(in);

		List<LongRef> initialValues = readRefs(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.initializeArray(arrayType, initialValues);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondGetArrayLength(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayRef = readRef(in);

		int result = communicator.getArrayLength(arrayRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		out.writeInt(result);
	}

	private void respondGetArrayElement(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayRef = readRef(in);
		int index = in.readInt();

		OperationOutcome<LongRef, Void, LongRef> result = communicator.getArrayElement(arrayRef, index);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondSetArrayElement(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayRef = readRef(in);
		int index = in.readInt();
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, Void, LongRef> result = communicator.setArrayElement(arrayRef, index, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidResOperationOutcome(out, result);
	}

	private void respondLookupConstructor(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		List<LongRef> params = readRefs(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.lookupConstructor(type, params);

		if(result.kind() == Kind.RESULT)
			constructorParamCounts.put(((OperationOutcome.Result<LongRef, Void, LongRef>) result).returnValue(), params.size());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondLookupMethod(DataInput in, DataOutput out, boolean isStatic) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef returnType = readRef(in);
		List<LongRef> params = readRefs(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.lookupMethod(type, name, returnType, params, isStatic);

		if(result.kind() == Kind.RESULT)
			methodParamCounts.put(((OperationOutcome.Result<LongRef, Void, LongRef>) result).returnValue(), params.size());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondLookupField(DataInput in, DataOutput out, boolean isStatic) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef fieldType = readRef(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.lookupField(type, name, fieldType, isStatic);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondCallConstructor(DataInput in, DataOutput out) throws IOException
	{
		LongRef constructor = readRef(in);
		List<LongRef> args = readNRefs(in, constructorParamCounts.get(constructor));

		OperationOutcome<LongRef, LongRef, LongRef> result = communicator.callConstructor(constructor, args);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}

	private void respondCallStaticMethod(DataInput in, DataOutput out) throws IOException
	{
		LongRef method = readRef(in);
		List<LongRef> args = readNRefs(in, methodParamCounts.get(method));

		OperationOutcome<LongRef, LongRef, LongRef> result = communicator.callStaticMethod(method, args);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondGetStaticField(DataInput in, DataOutput out) throws IOException
	{
		LongRef field = readRef(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.getStaticField(field);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}
	private void respondSetStaticField(DataInput in, DataOutput out) throws IOException
	{
		LongRef field = readRef(in);
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, Void, LongRef> result = communicator.setStaticField(field, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidResOperationOutcome(out, result);
	}

	private void respondCallInstanceMethod(DataInput in, DataOutput out) throws IOException
	{
		LongRef method = readRef(in);
		LongRef receiverRef = readRef(in);
		List<LongRef> args = readNRefs(in, methodParamCounts.get(method));

		OperationOutcome<LongRef, LongRef, LongRef> result = communicator.callInstanceMethod(method, receiverRef, args);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondGetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		LongRef field = readRef(in);
		LongRef receiverRef = readRef(in);

		OperationOutcome<LongRef, Void, LongRef> result = communicator.getInstanceField(field, receiverRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}
	private void respondSetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		LongRef field = readRef(in);
		LongRef receiverRef = readRef(in);
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, Void, LongRef> result = communicator.setInstanceField(field, receiverRef, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidResOperationOutcome(out, result);
	}

	private void respondSend(DataInput in, DataOutput out) throws IOException
	{
		LongRef serdesRef = readRef(in);
		DataInputStream serdesIn = getExerciseSideThread().data().in();

		LongRef result = communicator.getTransceiver().send(serdesRef, serdesIn);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondReceive(DataInput in, DataOutputStream out) throws IOException
	{
		LongRef serdesRef = readRef(in);
		LongRef objRef = readRef(in);
		DataOutputStream serdesOut = getExerciseSideThread().data().out();

		writeThreadResponse(out, STUDENT_FINISHED);
		out.flush();
		communicator.getTransceiver().receive(serdesRef, objRef, serdesOut);
		serdesOut.flush();
	}

	private void respondCreateCallbackInstance(DataInput in, DataOutput out) throws IOException
	{
		LongRef callbackRef = readRef(in);
		String interfaceCn = in.readUTF();

		communicator.getCallbackManager().createCallbackInstance(callbackRef, interfaceCn);

		writeThreadResponse(out, STUDENT_FINISHED);
	}

	private void respondNewThread(DataInputStream commandIn) throws ClosedException, IOException
	{
		// Exercise side cares about making sure this'll link the correct exchanges
		DataExchange control = createDataExchange();
		DataExchange data = createDataExchange();

		new Thread(() ->
		{
			try
			{
				try
				{
					threads.set(new ExerciseSideThread(control, data));
					handleExerciseSideCommandsUntilFinished(control);
				} catch(RuntimeException | Error e)
				{
					if(e instanceof OutOfMemoryError oome)
						outOfMemory(oome);
					else
						crashed(e);
					throw e;
				}
			} catch(OutOfMemoryError e)
			{
				// Handle OOME in an additional try-catch block because in the worst case,
				// an OOME occurs while handling a RuntimeException or Error.
				// That's kind of realistic if a student-caused OOME gets wrapped in some other exception.
				outOfMemory(e);
				throw e;
			}
		}).start();
	}

	private void respondRefDeleted(DataInput in) throws IOException
	{
		throw new UnsupportedOperationException("refDeleted currently unused");
		//		long deletedRef = readRef(in);
		//		int receivedCount = in.readInt();
		//		
		//		refManager.refDeleted(deletedRef, receivedCount);
	}

	private String getCallbackInterfaceCn(LongRef callbackRef) throws IOException
	{
		ExerciseSideThread exerciseSideThread = getExerciseSideThread();
		DataInput in = exerciseSideThread.control().in();
		DataOutputStream out = exerciseSideThread.control().out();

		writeThreadResponse(out, GET_CALLBACK_INTERFACE_CN);
		writeRef(out, callbackRef);
		out.flush();

		if(readThreadCommand(in) != EXERCISE_FINISHED)
			throw new IllegalStateException("Exercise side didn't respond with " + EXERCISE_FINISHED);
		return in.readUTF();
	}
	private CallbackOperationOutcome<LongRef, LongRef> callCallbackInstanceMethod(LongRef type, String name, LongRef returnType, List<LongRef> params,
			LongRef receiverRef, List<LongRef> argRefs) throws IOException
	{
		ExerciseSideThread exerciseSideThread = getExerciseSideThread();
		DataInput in = exerciseSideThread.control().in();
		DataOutputStream out = exerciseSideThread.control().out();

		writeThreadResponse(out, CALL_CALLBACK_INSTANCE_METHOD);
		writeRef(out, type);
		out.writeUTF(name);
		writeRef(out, returnType);
		int paramCount = writeRefs(out, params);
		writeRef(out, receiverRef);
		writeNRefs(out, argRefs, paramCount);

		out.flush();

		handleExerciseSideCommandsUntilFinished(exerciseSideThread.control());
		CallbackOperationOutcome.Kind kind = CallbackOperationOutcome.Kind.decode(in.readByte());
		return switch(kind)
		{
			case CALLBACK_RESULT -> new CallbackOperationOutcome.Result<>(readRef(in));
			case CALLBACK_THROWN -> new CallbackOperationOutcome.Thrown<>(readRef(in));
			case CALLBACK_HIDDEN_ERROR -> new CallbackOperationOutcome.HiddenError<>();
		};
	}

	private ThreadCommand readThreadCommand(DataInput in) throws IOException
	{
		return ThreadCommand.decode(in.readByte());
	}

	private void writeThreadResponse(DataOutput out, ThreadResponse threadResponse) throws IOException
	{
		out.writeByte(threadResponse.encode());
	}

	private List<LongRef> readRefs(DataInput in) throws IOException
	{
		return DataCommunicatorUtils.readList(in, this::readRef);
	}
	private List<LongRef> readNRefs(DataInput in, int n) throws IOException
	{
		return DataCommunicatorUtils.readN(in, n, this::readRef);
	}
	private int writeRefs(DataOutput out, List<LongRef> refs) throws IOException
	{
		return DataCommunicatorUtils.writeList(out, refs, this::writeRef);
	}
	private void writeNRefs(DataOutput out, List<LongRef> refs, int n) throws IOException
	{
		DataCommunicatorUtils.writeN(out, refs, n, IllegalArgumentException::new, this::writeRef);
	}

	protected final LongRef readRef(DataInput in) throws IOException
	{
		return refManager.unmarshalReceivedId(in.readLong());
	}
	protected final void writeVoidResOperationOutcome(DataOutput out, OperationOutcome<Void, Void, LongRef> outcome) throws IOException
	{
		writeOperationOutcome(out, outcome, (o, v) ->
		{}, (o, r) ->
		{});
	}
	protected final void writeVoidOperationOutcome(DataOutput out, OperationOutcome<LongRef, Void, LongRef> outcome) throws IOException
	{
		writeOperationOutcome(out, outcome, this::writeRef, (o, r) ->
		{});
	}
	protected final void writeOperationOutcome(DataOutput out, OperationOutcome<LongRef, LongRef, LongRef> outcome) throws IOException
	{
		writeOperationOutcome(out, outcome, this::writeRef, this::writeRef);
	}
	private final <R, T> void writeOperationOutcome(DataOutput out, OperationOutcome<R, T, LongRef> outcome,
			IOBiConsumer<DataOutput, R> writeRef, IOBiConsumer<DataOutput, T> writeThrowable) throws IOException
	{
		out.writeByte(outcome.kind().encode());
		switch(outcome.kind())
		{
			case RESULT -> writeRef.accept(out, ((OperationOutcome.Result<R, T, LongRef>) outcome).returnValue());
			case SUCCESS_WITHOUT_RESULT ->
			{
				// nothing to do
			}
			case THROWN -> writeThrowable.accept(out, ((OperationOutcome.Thrown<R, T, LongRef>) outcome).thrownThrowable());
			case CLASS_NOT_FOUND -> out.writeUTF(((OperationOutcome.ClassNotFound<R, T, LongRef>) outcome).classname());
			case FIELD_NOT_FOUND ->
			{
				OperationOutcome.FieldNotFound<R, T, LongRef> fieldNotFound =
						(OperationOutcome.FieldNotFound<R, T, LongRef>) outcome;
				writeRef(out, fieldNotFound.type());
				out.writeUTF(fieldNotFound.fieldName());
				writeRef(out, fieldNotFound.fieldType());
				out.writeBoolean(fieldNotFound.isStatic());
			}
			case METHOD_NOT_FOUND ->
			{
				OperationOutcome.MethodNotFound<R, T, LongRef> methodNotFound =
						(OperationOutcome.MethodNotFound<R, T, LongRef>) outcome;
				writeRef(out, methodNotFound.type());
				out.writeUTF(methodNotFound.methodName());
				writeRef(out, methodNotFound.returnType());
				writeRefs(out, methodNotFound.parameters());
				out.writeBoolean(methodNotFound.isStatic());
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				OperationOutcome.ConstructorNotFound<R, T, LongRef> constructorNotFound =
						(OperationOutcome.ConstructorNotFound<R, T, LongRef>) outcome;
				writeRef(out, constructorNotFound.type());
				writeRefs(out, constructorNotFound.parameters());
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED ->
			{
				OperationOutcome.ConstructorOfAbstractClassCreated<R, T, LongRef> constructorOfAbstractClassCalled =
						(OperationOutcome.ConstructorOfAbstractClassCreated<R, T, LongRef>) outcome;
				writeRef(out, constructorOfAbstractClassCalled.type());
				writeRefs(out, constructorOfAbstractClassCalled.parameters());
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				OperationOutcome.ArrayIndexOutOfBounds<R, T, LongRef> arrayIndexOutOfBounds =
						(OperationOutcome.ArrayIndexOutOfBounds<R, T, LongRef>) outcome;
				out.writeInt(arrayIndexOutOfBounds.index());
				out.writeInt(arrayIndexOutOfBounds.length());
			}
			case ARRAY_SIZE_NEGATIVE -> out.writeInt(((OperationOutcome.ArraySizeNegative<R, T, LongRef>) outcome).size());
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY ->
			{
				ArraySizeNegativeInMultiArray<R, T, LongRef> arraySizeNegativeInMultiArray =
						(OperationOutcome.ArraySizeNegativeInMultiArray<R, T, LongRef>) outcome;
				DataCommunicatorUtils.writeList(out, arraySizeNegativeInMultiArray.dimensions(), DataOutput::writeInt);
			}
		};
	}
	protected final void writeRef(DataOutput out, LongRef ref) throws IOException
	{
		out.writeLong(refManager.marshalRefForSending(ref));
	}

	private ExerciseSideThread getExerciseSideThread()
	{
		return threads.get();
	}

	private DataExchange createDataExchange() throws IOException
	{
		//TODO make wrapBuffered configurable
		return createDataExchange(true);
	}

	private DataExchange createDataExchange(boolean wrapBuffered) throws IOException
	{
		Exchange exchange = exchangePool.createNewExchange();
		if(wrapBuffered)
			exchange = exchange.wrapBuffered();
		return exchange.wrapData();
	}

	private static record ExerciseSideThread(DataExchange control, DataExchange data)
	{}
}
