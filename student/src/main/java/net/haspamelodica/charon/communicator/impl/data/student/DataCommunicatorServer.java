package net.haspamelodica.charon.communicator.impl.data.student;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadIndependentResponse.SHUTDOWN_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.CALL_CALLBACK_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.GET_CALLBACK_INTERFACE_CN;
import static net.haspamelodica.charon.communicator.impl.data.ThreadResponse.STUDENT_FINISHED;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegativeInMultiArray;
import net.haspamelodica.charon.communicator.ExternalCallbackManager;
import net.haspamelodica.charon.communicator.ServerSideTransceiver;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorConstants;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils.Args;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.ThreadCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadResponse;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.refs.longref.LongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;
import net.haspamelodica.streammultiplexer.BufferedDataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.ClosedException;
import net.haspamelodica.streammultiplexer.DataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.MultiplexedDataInputStream;
import net.haspamelodica.streammultiplexer.MultiplexedDataOutputStream;

public class DataCommunicatorServer
{
	protected final DataStreamMultiplexer multiplexer;

	private final StudentSideCommunicator<LongRef, LongRef, ? extends ServerSideTransceiver<LongRef>,
			? extends ExternalCallbackManager<LongRef>> communicator;

	private final LongRefManager<LongRef> refManager;

	private final ThreadLocal<ExerciseSideThread> threads;

	private final AtomicBoolean running;

	public DataCommunicatorServer(InputStream rawIn, OutputStream rawOut, RefTranslatorCommunicatorSupplier<LongRef,
			ServerSideTransceiver<LongRef>, ExternalCallbackManager<LongRef>> communicatorSupplier)
	{
		// write magic number for "no compilation error"
		try
		{
			rawOut.write('s');
		} catch(IOException e)
		{
			throw new UncheckedIOException("Error while writing compilation error marker", e);
		}

		this.multiplexer = new BufferedDataStreamMultiplexer(rawIn, rawOut);
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
		this.threads = new ThreadLocal<>();
		this.running = new AtomicBoolean();
	}

	// Don't even try to catch IOExceptions; just crash.
	// Exercise has to handle this correctly anyway as this behaviour could also created maliciously.
	public void run() throws IOException
	{
		running.set(true);
		MultiplexedDataInputStream commandIn = multiplexer.getIn(DataCommunicatorConstants.THREAD_INDEPENDENT_COMMAND_STERAM_ID);
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
			MultiplexedDataOutputStream commandOut = multiplexer.getOut(DataCommunicatorConstants.THREAD_INDEPENDENT_COMMAND_STERAM_ID);
			commandOut.writeByte(SHUTDOWN_FINISHED.encode());
			commandOut.flush();
			multiplexer.close();
		} catch(RuntimeException e)
		{
			//TODO log to somewhere instead of rethrowing
			throw e;
		}
	}

	private void handleExerciseSideCommandsUntilFinished(DataInput in, DataOutputStream out)
	{
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
					case NEW_ARRAY -> respondNewArray(in, out);
					case NEW_MULTI_ARRAY -> respondNewMultiArray(in, out);
					case NEW_ARRAY_WITH_INITIAL_VALUES -> respondNewArrayWithInitialValues(in, out);
					case GET_ARRAY_LENGTH -> respondGetArrayLength(in, out);
					case GET_ARRAY_ELEMENT -> respondGetArrayElement(in, out);
					case SET_ARRAY_ELEMENT -> respondSetArrayElement(in, out);
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
			// I don't think so because once StreamMultiplexer or one of its streams throws any exception,
			// it will continue to throw that exception forever
			throw new UncheckedIOException(e);
		}
	}

	private void respondGetTypeByName(DataInput in, DataOutput out) throws IOException
	{
		String typeName = in.readUTF();

		OperationOutcome<LongRef, LongRef> result = communicator.getTypeByName(typeName);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
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

		int superinterfacesCount = result.superinterfaces().size();
		out.writeInt(superinterfacesCount);
		// index-based iteration to defend against botched List implementations
		for(int i = 0; i < superinterfacesCount; i ++)
			writeRef(out, result.superinterfaces().get(i));

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

		OperationOutcome<LongRef, LongRef> result = communicator.newArray(arrayType, length);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}

	private void respondNewMultiArray(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayType = readRef(in);

		int dimensionsSize = in.readInt();
		Integer[] dimensions = new Integer[dimensionsSize];
		for(int i = 0; i < dimensionsSize; i ++)
			dimensions[i] = in.readInt();

		OperationOutcome<LongRef, LongRef> result = communicator.newMultiArray(arrayType, List.of(dimensions));

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}

	private void respondNewArrayWithInitialValues(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayType = readRef(in);

		int length = in.readInt();
		LongRef[] initialValues = new LongRef[length];
		for(int i = 0; i < length; i ++)
			initialValues[i] = readRef(in);

		OperationOutcome<LongRef, LongRef> result = communicator.newArrayWithInitialValues(arrayType, List.of(initialValues));

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
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

		OperationOutcome<LongRef, LongRef> result = communicator.getArrayElement(arrayRef, index);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}

	private void respondSetArrayElement(DataInput in, DataOutput out) throws IOException
	{
		LongRef arrayRef = readRef(in);
		int index = in.readInt();
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, LongRef> result = communicator.setArrayElement(arrayRef, index, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondCallConstructor(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		Args args = readArgs(in);

		OperationOutcome<LongRef, LongRef> result = communicator.callConstructor(type, args.params(), args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}

	private void respondCallStaticMethod(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef returnType = readRef(in);
		Args args = readArgs(in);

		OperationOutcome<LongRef, LongRef> result = communicator.callStaticMethod(type, name, returnType, args.params(), args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondGetStaticField(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef fieldType = readRef(in);

		OperationOutcome<LongRef, LongRef> result = communicator.getStaticField(type, name, fieldType);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondSetStaticField(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef fieldType = readRef(in);
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, LongRef> result = communicator.setStaticField(type, name, fieldType, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondCallInstanceMethod(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef returnType = readRef(in);
		Args args = readArgs(in);
		LongRef receiverRef = readRef(in);

		OperationOutcome<LongRef, LongRef> result = communicator.callInstanceMethod(type, name, returnType, args.params(), receiverRef, args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondGetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef fieldType = readRef(in);
		LongRef receiverRef = readRef(in);

		OperationOutcome<LongRef, LongRef> result = communicator.getInstanceField(type, name, fieldType, receiverRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeOperationOutcome(out, result);
	}
	private void respondSetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		LongRef type = readRef(in);
		String name = in.readUTF();
		LongRef fieldType = readRef(in);
		LongRef receiverRef = readRef(in);
		LongRef valueRef = readRef(in);

		OperationOutcome<Void, LongRef> result = communicator.setInstanceField(type, name, fieldType, receiverRef, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeVoidOperationOutcome(out, result);
	}

	private void respondSend(DataInput in, DataOutput out) throws IOException
	{
		LongRef serdesRef = readRef(in);
		int serdesInID = in.readInt();

		LongRef result = communicator.getTransceiver().send(serdesRef, multiplexer.getIn(serdesInID));

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondReceive(DataInput in, DataOutputStream out) throws IOException
	{
		LongRef serdesRef = readRef(in);
		LongRef objRef = readRef(in);
		MultiplexedDataOutputStream serdesOut = multiplexer.getOut(in.readInt());

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

	private void respondNewThread(MultiplexedDataInputStream commandIn) throws ClosedException, IOException
	{
		MultiplexedDataOutputStream out = multiplexer.getOut(commandIn.readInt());
		MultiplexedDataInputStream in = multiplexer.getIn(commandIn.readInt());

		new Thread(() ->
		{
			threads.set(new ExerciseSideThread(in, out));
			handleExerciseSideCommandsUntilFinished(in, out);
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
		DataInput in = exerciseSideThread.in();
		DataOutputStream out = exerciseSideThread.out();

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
		DataInput in = exerciseSideThread.in();
		DataOutputStream out = exerciseSideThread.out();

		writeThreadResponse(out, CALL_CALLBACK_INSTANCE_METHOD);
		writeRef(out, type);
		out.writeUTF(name);
		writeRef(out, returnType);
		writeArgs(out, params, argRefs);
		writeRef(out, receiverRef);

		out.flush();

		handleExerciseSideCommandsUntilFinished(in, out);
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

	private Args readArgs(DataInput in) throws IOException
	{
		return DataCommunicatorUtils.readArgs(in, this::readRef);
	}
	private void writeArgs(DataOutput out, List<LongRef> params, List<LongRef> argRefs) throws IOException
	{
		DataCommunicatorUtils.writeArgs(out, params, argRefs, IllegalArgumentException::new, this::writeRef);
	}

	protected final LongRef readRef(DataInput in) throws IOException
	{
		return refManager.unmarshalReceivedId(in.readLong());
	}
	protected final void writeVoidOperationOutcome(DataOutput out, OperationOutcome<Void, LongRef> outcome) throws IOException
	{
		writeOperationOutcome(out, outcome, (o, v) ->
		{});
	}
	protected final void writeOperationOutcome(DataOutput out, OperationOutcome<LongRef, LongRef> outcome) throws IOException
	{
		writeOperationOutcome(out, outcome, this::writeRef);
	}
	private final <R> void writeOperationOutcome(DataOutput out, OperationOutcome<R, LongRef> outcome,
			IOBiConsumer<DataOutput, R> writeRef) throws IOException
	{
		out.writeByte(outcome.kind().encode());
		switch(outcome.kind())
		{
			case RESULT -> writeRef.accept(out, ((OperationOutcome.Result<R, LongRef>) outcome).returnValue());
			case SUCCESS_WITHOUT_RESULT ->
			{
				// nothing to do
			}
			case THROWN -> writeRef.accept(out, ((OperationOutcome.Thrown<R, LongRef>) outcome).thrownThrowable());
			case CLASS_NOT_FOUND -> out.writeUTF(((OperationOutcome.ClassNotFound<R, LongRef>) outcome).classname());
			case FIELD_NOT_FOUND ->
			{
				OperationOutcome.FieldNotFound<R, LongRef> fieldNotFound =
						(OperationOutcome.FieldNotFound<R, LongRef>) outcome;
				writeRef(out, fieldNotFound.type());
				out.writeUTF(fieldNotFound.fieldName());
				writeRef(out, fieldNotFound.fieldType());
				out.writeBoolean(fieldNotFound.isStatic());
			}
			case METHOD_NOT_FOUND ->
			{
				OperationOutcome.MethodNotFound<R, LongRef> methodNotFound =
						(OperationOutcome.MethodNotFound<R, LongRef>) outcome;
				writeRef(out, methodNotFound.type());
				out.writeUTF(methodNotFound.methodName());
				writeRef(out, methodNotFound.returnType());
				List<LongRef> parameters = methodNotFound.parameters();
				int parametersSize = parameters.size();
				out.writeInt(parametersSize);
				for(int i = 0; i < parametersSize; i ++)
					writeRef(out, parameters.get(i));
				out.writeBoolean(methodNotFound.isStatic());
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				OperationOutcome.ConstructorNotFound<R, LongRef> constructorNotFound =
						(OperationOutcome.ConstructorNotFound<R, LongRef>) outcome;
				writeRef(out, constructorNotFound.type());
				List<LongRef> parameters = constructorNotFound.parameters();
				int parametersSize = parameters.size();
				out.writeInt(parametersSize);
				for(int i = 0; i < parametersSize; i ++)
					writeRef(out, parameters.get(i));
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED ->
			{
				OperationOutcome.ConstructorOfAbstractClassCalled<R, LongRef> constructorOfAbstractClassCalled =
						(OperationOutcome.ConstructorOfAbstractClassCalled<R, LongRef>) outcome;
				writeRef(out, constructorOfAbstractClassCalled.type());
				List<LongRef> parameters = constructorOfAbstractClassCalled.parameters();
				int parametersSize = parameters.size();
				out.writeInt(parametersSize);
				for(int i = 0; i < parametersSize; i ++)
					writeRef(out, parameters.get(i));
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				OperationOutcome.ArrayIndexOutOfBounds<R, LongRef> arrayIndexOutOfBounds =
						(OperationOutcome.ArrayIndexOutOfBounds<R, LongRef>) outcome;
				out.writeInt(arrayIndexOutOfBounds.index());
				out.writeInt(arrayIndexOutOfBounds.length());
			}
			case ARRAY_SIZE_NEGATIVE -> out.writeInt(((OperationOutcome.ArraySizeNegative<R, LongRef>) outcome).size());
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY ->
			{
				ArraySizeNegativeInMultiArray<R, LongRef> arraySizeNegativeInMultiArray =
						(OperationOutcome.ArraySizeNegativeInMultiArray<R, LongRef>) outcome;
				List<Integer> dimensions = arraySizeNegativeInMultiArray.dimensions();
				int dimensionsSize = dimensions.size();
				out.writeInt(dimensionsSize);
				for(int i = 0; i < dimensionsSize; i ++)
					out.writeInt(dimensions.get(i));
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

	private static record ExerciseSideThread(MultiplexedDataInputStream in, MultiplexedDataOutputStream out)
	{}
}
