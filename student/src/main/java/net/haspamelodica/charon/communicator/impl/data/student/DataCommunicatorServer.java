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

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorConstants;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils;
import net.haspamelodica.charon.communicator.impl.data.DataCommunicatorUtils.Args;
import net.haspamelodica.charon.communicator.impl.data.ThreadCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.charon.communicator.impl.data.ThreadResponse;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorServerSideSupplier;
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

	private final StudentSideCommunicatorServerSide<LongRef> communicator;

	private final LongRefManager<LongRef> refManager;

	private final ThreadLocal<ExerciseSideThread> threads;

	private final AtomicBoolean running;

	public DataCommunicatorServer(InputStream rawIn, OutputStream rawOut, RefTranslatorCommunicatorServerSideSupplier communicatorSupplier)
	{
		this.multiplexer = new BufferedDataStreamMultiplexer(rawIn, rawOut);
		this.refManager = new SimpleLongRefManager(true);
		this.communicator = communicatorSupplier.createCommunicator(false, new RefTranslatorCommunicatorCallbacks<>()
		{
			@Override
			public <REF_FROM> LongRef createForwardRef(UntranslatedRef<REF_FROM> untranslatedRef)
			{
				return refManager.createManagedRef();
			}

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
			public LongRef callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
					LongRef receiverRef, List<LongRef> argRefs)
			{
				try
				{
					return DataCommunicatorServer.this.callCallbackInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
				} catch(IOException e)
				{
					// If there's an IOException while communicating with the exercise side, nothing matters anymore.
					throw new UncheckedIOException(e);
				}
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
					case GET_CLASSNAME -> respondGetClassname(in, out);
					case GET_SUPERCLASS -> respondGetSuperclass(in, out);
					case GET_INTERFACES -> respondGetInterfaces(in, out);
					case SEND -> respondSend(in, out);
					case RECEIVE -> respondReceive(in, out);
					case CALL_CONSTRUCTOR -> respondCallConstructor(in, out);
					case CALL_STATIC_METHOD -> respondCallStaticMethod(in, out);
					case GET_STATIC_FIELD -> respondGetStaticField(in, out);
					case SET_STATIC_FIELD -> respondSetStaticField(in, out);
					case CALL_INSTANCE_METHOD -> respondCallInstanceMethod(in, out);
					case GET_INSTANCE_FIELD -> respondGetInstanceField(in, out);
					case SET_INSTANCE_FIELD -> respondSetInstanceField(in, out);
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

	private void respondGetClassname(DataInput in, DataOutput out) throws IOException
	{
		LongRef ref = readRef(in);

		writeThreadResponse(out, STUDENT_FINISHED);
		out.writeUTF(communicator.getClassname(ref));
	}

	private void respondGetSuperclass(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();

		writeThreadResponse(out, STUDENT_FINISHED);
		out.writeUTF(communicator.getSuperclass(cn));
	}

	private void respondGetInterfaces(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();

		List<String> interfaces = communicator.getInterfaces(cn);

		writeThreadResponse(out, STUDENT_FINISHED);
		out.writeInt(interfaces.size());
		for(String interfaceCn : interfaces)
			out.writeUTF(interfaceCn);
	}

	private void respondCallConstructor(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		Args args = readArgs(in);

		LongRef result = communicator.callConstructor(cn, args.params(), args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondCallStaticMethod(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String returnClassname = in.readUTF();
		Args args = readArgs(in);

		LongRef result = communicator.callStaticMethod(cn, name, returnClassname, args.params(), args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}
	private void respondGetStaticField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();

		LongRef result = communicator.getStaticField(cn, name, fieldClassname);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}
	private void respondSetStaticField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		LongRef valueRef = readRef(in);

		communicator.setStaticField(cn, name, fieldClassname, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
	}

	private void respondCallInstanceMethod(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String returnClassname = in.readUTF();
		Args args = readArgs(in);
		LongRef receiverRef = readRef(in);

		LongRef result = communicator.callInstanceMethod(cn, name, returnClassname, args.params(), receiverRef, args.argRefs());

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}
	private void respondGetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		LongRef receiverRef = readRef(in);

		LongRef result = communicator.getInstanceField(cn, name, fieldClassname, receiverRef);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}
	private void respondSetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		LongRef receiverRef = readRef(in);
		LongRef valueRef = readRef(in);

		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);

		writeThreadResponse(out, STUDENT_FINISHED);
	}

	private void respondCreateCallbackInstance(DataInput in, DataOutput out) throws IOException
	{
		String interfaceCn = in.readUTF();

		LongRef result = communicator.createCallbackInstance(interfaceCn);

		writeThreadResponse(out, STUDENT_FINISHED);
		writeRef(out, result);
	}

	private void respondSend(DataInput in, DataOutput out) throws IOException
	{
		LongRef serdesRef = readRef(in);
		int serdesInID = in.readInt();

		LongRef result = communicator.send(serdesRef, multiplexer.getIn(serdesInID));

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
		communicator.receive(serdesRef, objRef, serdesOut);
		serdesOut.flush();
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
	private LongRef callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			LongRef receiverRef, List<LongRef> argRefs) throws IOException
	{
		ExerciseSideThread exerciseSideThread = getExerciseSideThread();
		DataInput in = exerciseSideThread.in();
		DataOutputStream out = exerciseSideThread.out();

		writeThreadResponse(out, CALL_CALLBACK_INSTANCE_METHOD);
		out.writeUTF(cn);
		out.writeUTF(name);
		out.writeUTF(returnClassname);
		writeArgs(out, params, argRefs);
		writeRef(out, receiverRef);

		out.flush();

		handleExerciseSideCommandsUntilFinished(in, out);
		return readRef(in);
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
	private void writeArgs(DataOutput out, List<String> params, List<LongRef> argRefs) throws IOException
	{
		DataCommunicatorUtils.writeArgs(out, params, argRefs, IllegalArgumentException::new, this::writeRef);
	}

	protected final LongRef readRef(DataInput in) throws IOException
	{
		return refManager.unmarshalReceivedId(in.readLong());
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
