package net.haspamelodica.charon.communicator.impl.data.exercise;

import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_CONSTRUCTOR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CALL_STATIC_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_ARRAY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_CALLBACK_INSTANCE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.CREATE_MULTI_ARRAY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.DESCRIBE_TYPE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.EXERCISE_FINISHED;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_ELEMENT;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_LENGTH;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_ARRAY_TYPE;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_BY_NAME;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_HANDLED_BY_SERDES;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.GET_TYPE_OF;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.INITIALIZE_ARRAY;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.LOOKUP_CONSTRUCTOR;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.LOOKUP_INSTANCE_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.LOOKUP_INSTANCE_METHOD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.LOOKUP_STATIC_FIELD;
import static net.haspamelodica.charon.communicator.impl.data.ThreadCommand.LOOKUP_STATIC_METHOD;
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
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.OperationOutcome.Kind;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils;
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
import net.haspamelodica.charon.utils.IOBiFunction;
import net.haspamelodica.charon.utils.IOConsumer;
import net.haspamelodica.charon.utils.IOFunction;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;
import net.haspamelodica.exchanges.DataExchange;
import net.haspamelodica.exchanges.ExchangePool;
import net.haspamelodica.exchanges.multiplexed.ClosedException;
import net.haspamelodica.exchanges.multiplexed.UnexpectedResponseException;

public class DataCommunicatorClient
		implements StudentSideCommunicator<LongRef, LongRef, LongRef, LongRef, LongRef, LongRef,
				ClientSideTransceiver<LongRef>, InternalCallbackManager<LongRef>>,
		ClientSideTransceiver<LongRef>,
		InternalCallbackManager<LongRef>
{
	private final StudentSideCommunicatorCallbacks<LongRef, LongRef, LongRef> callbacks;

	private final ExchangePool exchangePool;

	private final LongRefManager<LongRef> refManager;

	// Caching these param counts saves 4 bytes per method / constructor call, but needs to look up in these maps.
	// Experimentally, this doesn't seem to make much of a difference.
	private final UnidirectionalMap<LongRef, Integer>	constructorParamCounts;
	private final UnidirectionalMap<LongRef, Integer>	methodParamCounts;

	private final Object							commandExchangeLock;
	private final DataExchange						threadIndependentCommandExchange;
	private final ThreadLocal<StudentSideThread>	threads;

	public DataCommunicatorClient(ExchangePool exchangePool, StudentSideCommunicatorCallbacks<LongRef, LongRef, LongRef> callbacks)
	{
		this.exchangePool = exchangePool;
		this.callbacks = callbacks;

		this.refManager = new SimpleLongRefManager(true);
		this.constructorParamCounts = UnidirectionalMap.builder().concurrent().weakKeys().build();
		this.methodParamCounts = UnidirectionalMap.builder().concurrent().weakKeys().build();

		this.threads = new ThreadLocal<>();
		this.commandExchangeLock = new Object();
		try
		{
			this.threadIndependentCommandExchange = createDataExchange();
		} catch(IOException e)
		{
			throw new UncheckedIOException("Error while creating thread-independent command exchange", e);
		}
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
		try
		{
			exchangePool.close();
		} catch(IOException e)
		{
			throw new UncheckedIOException("Error while shutting down", e);
		}
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return false;
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> getTypeByName(String typeName)
	{
		return executeVoidOperation(GET_TYPE_BY_NAME, DISALLOW_CALLBACKS, out -> out.writeUTF(typeName));
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
			List<LongRef> superinterfaces = readRefs(in);
			Optional<LongRef> componentTypeIfArray = Optional.ofNullable(readRef(in));

			return new StudentSideTypeDescription<>(kind, name, superclass, superinterfaces, componentTypeIfArray);
		});
	}

	@Override
	public LongRef getTypeHandledBySerdes(LongRef serdesRef)
	{
		return executeRefCommand(GET_TYPE_HANDLED_BY_SERDES, ALLOW_CALLBACKS, out -> writeRef(out, serdesRef));
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> createArray(LongRef arrayType, int length)
	{
		return executeVoidOperation(CREATE_ARRAY, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayType);
			out.writeInt(length);
		});
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> createMultiArray(LongRef arrayType, List<Integer> dimensions)
	{
		return executeVoidOperation(CREATE_MULTI_ARRAY, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayType);
			DataCommunicatorUtils.writeList(out, dimensions, DataOutput::writeInt);
		});
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> initializeArray(LongRef arrayType, List<LongRef> initialValues)
	{
		return executeVoidOperation(INITIALIZE_ARRAY, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayType);
			writeRefs(out, initialValues);
		});
	}

	@Override
	public int getArrayLength(LongRef arrayRef)
	{
		return executeCommand(GET_ARRAY_LENGTH, DISALLOW_CALLBACKS, out -> writeRef(out, arrayRef), DataInput::readInt);
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> getArrayElement(LongRef arrayRef, int index)
	{
		return executeVoidOperation(GET_ARRAY_ELEMENT, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayRef);
			out.writeInt(index);
		});
	}

	@Override
	public OperationOutcome<Void, Void, LongRef> setArrayElement(LongRef arrayRef, int index, LongRef valueRef)
	{
		return executeVoidResOperation(SET_ARRAY_ELEMENT, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, arrayRef);
			out.writeInt(index);
			writeRef(out, valueRef);
		});
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> lookupConstructor(LongRef type, List<LongRef> params)
	{
		int paramsCount = params.size();
		OperationOutcome<LongRef, Void, LongRef> result = executeVoidOperation(LOOKUP_CONSTRUCTOR, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			// Use paramsCount here to defend against botched List implementations: this causes size to be called only once
			out.writeInt(paramsCount);
			writeNRefsUnchecked(out, params, paramsCount);
		});

		if(result.kind() == Kind.RESULT)
			constructorParamCounts.put(((OperationOutcome.Result<LongRef, Void, LongRef>) result).returnValue(), paramsCount);

		return result;
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> lookupMethod(LongRef type, String name, LongRef returnType, List<LongRef> params, boolean isStatic)
	{
		int paramsCount = params.size();

		ThreadCommand command = isStatic ? LOOKUP_STATIC_METHOD : LOOKUP_INSTANCE_METHOD;
		OperationOutcome<LongRef, Void, LongRef> result = executeVoidOperation(command, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, returnType);
			// Use paramsCount here to defend against botched List implementations: this causes size to be called only once
			out.writeInt(paramsCount);
			writeNRefsUnchecked(out, params, paramsCount);
		});

		if(result.kind() == Kind.RESULT)
			methodParamCounts.put(((OperationOutcome.Result<LongRef, Void, LongRef>) result).returnValue(), paramsCount);

		return result;
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> lookupField(LongRef type, String name, LongRef fieldType, boolean isStatic)
	{
		ThreadCommand command = isStatic ? LOOKUP_STATIC_FIELD : LOOKUP_INSTANCE_FIELD;
		return executeVoidOperation(command, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, type);
			out.writeUTF(name);
			writeRef(out, fieldType);
		});
	}

	@Override
	public OperationOutcome<LongRef, LongRef, LongRef> callConstructor(LongRef constructor, List<LongRef> argRefs)
	{
		return executeOperation(CALL_CONSTRUCTOR, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, constructor);
			writeNRefs(out, argRefs, constructorParamCounts.get(constructor));
		});
	}

	@Override
	public OperationOutcome<LongRef, LongRef, LongRef> callStaticMethod(LongRef method, List<LongRef> argRefs)
	{
		return executeOperation(CALL_STATIC_METHOD, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, method);
			writeNRefs(out, argRefs, methodParamCounts.get(method));
		});
	}

	@Override
	public OperationOutcome<LongRef, Void, LongRef> getStaticField(LongRef field)
	{
		return executeVoidOperation(GET_STATIC_FIELD, DISALLOW_CALLBACKS, out -> writeRef(out, field));
	}
	@Override
	public OperationOutcome<Void, Void, LongRef> setStaticField(LongRef field, LongRef valueRef)
	{
		return executeVoidResOperation(SET_STATIC_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, field);
			writeRef(out, valueRef);
		});
	}

	@Override
	public OperationOutcome<LongRef, LongRef, LongRef> callInstanceMethod(LongRef method, LongRef receiverRef, List<LongRef> argRefs)
	{
		return executeOperation(CALL_INSTANCE_METHOD, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, method);
			writeRef(out, receiverRef);
			writeNRefs(out, argRefs, methodParamCounts.get(method));
		});
	}
	@Override
	public OperationOutcome<LongRef, Void, LongRef> getInstanceField(LongRef field, LongRef receiverRef)
	{
		return executeVoidOperation(GET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, field);
			writeRef(out, receiverRef);
		});
	}
	@Override
	public OperationOutcome<Void, Void, LongRef> setInstanceField(LongRef field, LongRef receiverRef, LongRef valueRef)
	{
		return executeVoidResOperation(SET_INSTANCE_FIELD, DISALLOW_CALLBACKS, out ->
		{
			writeRef(out, field);
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
			writeRef(out, serdesRef);
			out.flush();
			DataOutputStream serdesOut = getStudentSideThread().data().out();
			serializer.serialize(serdesOut, obj);
			serdesOut.flush();
		});
	}

	@Override
	public <T> T receive(LongRef serdesRef, Deserializer<T> deserializer, LongRef objRef)
	{
		return executeCommand(RECEIVE, ALLOW_CALLBACKS, out ->
		{
			writeRef(out, serdesRef);
			writeRef(out, objRef);
		}, in -> deserializer.deserialize(getStudentSideThread().data().in()));
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
	private OperationOutcome<Void, Void, LongRef> executeVoidResOperation(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, allowedCallbacks, sendParams, this::readVoidResOperationOutcome);
	}
	private OperationOutcome<LongRef, Void, LongRef> executeVoidOperation(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, allowedCallbacks, sendParams, this::readVoidOperationOutcome);
	}
	private OperationOutcome<LongRef, LongRef, LongRef> executeOperation(ThreadCommand command, AllowedThreadCallbacks allowedCallbacks,
			IOConsumer<DataOutputStream> sendParams)
	{
		return executeCommand(command, allowedCallbacks, sendParams, this::readOperationOutcome);
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

			writeThreadCommand(thread.control().out(), command);
			P params = sendParams.apply(thread.control().out());
			thread.control().out().flush();

			handleStudentSideResponsesUntilFinished(allowedCallbacks, thread.control().in(), thread.control().out());
			return parseResponse.apply(thread.control().in(), params);
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
				case STUDENT_ERROR ->
				{
					//TODO transmit details; also, do we want to shut down the entire StudentSide in this case?
					throw new FrameworkCausedException("Student side crashed");
				}
				case CALL_CALLBACK_INSTANCE_METHOD ->
				{
					LongRef type = readRef(in);
					String name = in.readUTF();
					LongRef returnType = readRef(in);
					List<LongRef> params = readRefs(in);
					LongRef receiverRef = readRef(in);
					List<LongRef> args = readNRefs(in, params.size());

					//TODO if the result is a hidden error, we want the "outer" operation to always throw that exception.
					CallbackOperationOutcome<LongRef, LongRef> result = callbacks.callCallbackInstanceMethod(type, name, returnType, params,
							receiverRef, args);

					writeThreadCommand(out, EXERCISE_FINISHED);
					out.writeByte(result.kind().encode());
					switch(result.kind())
					{
						case CALLBACK_RESULT -> writeRef(out, ((CallbackOperationOutcome.Result<LongRef, LongRef>) result).returnValue());
						case CALLBACK_THROWN -> writeRef(out, ((CallbackOperationOutcome.Thrown<LongRef, LongRef>) result).thrownThrowable());
						case CALLBACK_HIDDEN_ERROR ->
						{
							// nothing to do
						}
					};
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

	private void executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutputStream> sendCommand)
	{
		executeThreadIndependentCommand(command, sendCommand, in -> null);
	}
	private <R> R executeThreadIndependentCommand(ThreadIndependentCommand command, IOConsumer<DataOutputStream> sendCommand,
			IOFunction<DataInput, R> parseResponse)
	{
		synchronized(commandExchangeLock)
		{
			try
			{
				threadIndependentCommandExchange.out().writeByte(command.encode());
				sendCommand.accept(threadIndependentCommandExchange.out());
				threadIndependentCommandExchange.out().flush();
				return parseResponse.apply(threadIndependentCommandExchange.in());
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
		return executeThreadIndependentCommand(NEW_THREAD, commandOut ->
		{}, commandIn ->
		{
			DataExchange cont = createDataExchange();
			DataExchange data = createDataExchange();
			return new StudentSideThread(cont, data);
		});
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

	private OperationOutcome<Void, Void, LongRef> readVoidResOperationOutcome(DataInput in) throws IOException
	{
		return readOperationOutcome(in, i -> null, i -> null);
	}
	private OperationOutcome<LongRef, Void, LongRef> readVoidOperationOutcome(DataInput in) throws IOException
	{
		return readOperationOutcome(in, this::readRef, i -> null);
	}
	private OperationOutcome<LongRef, LongRef, LongRef> readOperationOutcome(DataInput in) throws IOException
	{
		return readOperationOutcome(in, this::readRef, this::readRef);
	}
	private <R, T> OperationOutcome<R, T, LongRef> readOperationOutcome(DataInput in,
			IOFunction<DataInput, R> readRef, IOFunction<DataInput, T> readThrowable) throws IOException
	{
		OperationOutcome.Kind kind = OperationOutcome.Kind.decode(in.readByte());
		return switch(kind)
		{
			case RESULT -> new OperationOutcome.Result<>(readRef.apply(in));
			case SUCCESS_WITHOUT_RESULT -> new OperationOutcome.SuccessWithoutResult<>();
			case THROWN -> new OperationOutcome.Thrown<>(readThrowable.apply(in));
			case CLASS_NOT_FOUND -> new OperationOutcome.ClassNotFound<>(in.readUTF());
			case FIELD_NOT_FOUND ->
			{
				LongRef type = readRef(in);
				String fieldName = in.readUTF();
				LongRef fieldType = readRef(in);
				boolean isStatic = in.readBoolean();
				yield new OperationOutcome.FieldNotFound<>(type, fieldName, fieldType, isStatic);
			}
			case METHOD_NOT_FOUND ->
			{
				LongRef type = readRef(in);
				String methodName = in.readUTF();
				LongRef returnType = readRef(in);
				List<LongRef> parameters = readRefs(in);
				boolean isStatic = in.readBoolean();
				yield new OperationOutcome.MethodNotFound<>(type, methodName, returnType, parameters, isStatic);
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				LongRef type = readRef(in);
				List<LongRef> parameters = readRefs(in);
				yield new OperationOutcome.ConstructorNotFound<>(type, parameters);
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED ->
			{
				LongRef type = readRef(in);
				List<LongRef> parameters = readRefs(in);
				yield new OperationOutcome.ConstructorOfAbstractClassCreated<>(type, parameters);
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				int index = in.readInt();
				int length = in.readInt();
				yield new OperationOutcome.ArrayIndexOutOfBounds<>(index, length);
			}
			case ARRAY_SIZE_NEGATIVE -> new OperationOutcome.ArraySizeNegative<>(in.readInt());
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY ->
			{
				List<Integer> dimensions = DataCommunicatorUtils.readList(in, DataInput::readInt);
				yield new OperationOutcome.ArraySizeNegativeInMultiArray<>(dimensions);
			}
		};
	}

	private List<LongRef> readRefs(DataInput in) throws IOException
	{
		return DataCommunicatorUtils.readList(in, this::readRef);
	}
	private List<LongRef> readNRefs(DataInput in, int n) throws IOException
	{
		return DataCommunicatorUtils.readN(in, n, this::readRef);
	}
	private void writeRefs(DataOutput out, List<LongRef> refs) throws IOException
	{
		DataCommunicatorUtils.writeList(out, refs, this::writeRef);
	}
	private void writeNRefs(DataOutput out, List<LongRef> refs, int n) throws IOException
	{
		DataCommunicatorUtils.writeN(out, refs, n, IllegalArgumentException::new, this::writeRef);
	}
	private void writeNRefsUnchecked(DataOutput out, List<LongRef> refs, int n) throws IOException
	{
		DataCommunicatorUtils.writeNUnchecked(out, refs, n, this::writeRef);
	}

	private LongRef readRef(DataInput in) throws IOException
	{
		return refManager.unmarshalReceivedId(in.readLong());
	}
	private void writeRef(DataOutput out, LongRef ref) throws IOException
	{
		out.writeLong(refManager.marshalRefForSending(ref));
	}

	private DataExchange createDataExchange() throws IOException
	{
		//TODO make wrapBuffered configurable
		return exchangePool.createNewExchange().wrapBuffered().wrapData();
	}

	private static record StudentSideThread(DataExchange control, DataExchange data)
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
