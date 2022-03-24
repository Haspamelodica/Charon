package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import static net.haspamelodica.studentcodeseparator.communicator.impl.data.ThreadIndependentResponse.SHUTDOWN_FINISHED;
import static net.haspamelodica.studentcodeseparator.communicator.impl.data.ThreadResponse.SERIALIZER_READY;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.haspamelodica.streammultiplexer.BufferedDataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.ClosedException;
import net.haspamelodica.streammultiplexer.DataStreamMultiplexer;
import net.haspamelodica.streammultiplexer.MultiplexedDataInputStream;
import net.haspamelodica.streammultiplexer.MultiplexedDataOutputStream;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.ThreadCommand;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.ThreadIndependentCommand;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class DataCommunicatorServer<REF extends Ref<DataCommunicatorAttachment>>
{
	protected final DataStreamMultiplexer multiplexer;

	private final StudentSideCommunicatorServerSide<DataCommunicatorAttachment, REF> communicator;

	private final IDManager<REF> idManager;

	private final AtomicBoolean running;

	public DataCommunicatorServer(InputStream rawIn, OutputStream rawOut,
			StudentSideCommunicatorServerSide<DataCommunicatorAttachment, REF> communicator)
	{
		this.multiplexer = new BufferedDataStreamMultiplexer(rawIn, rawOut);

		this.communicator = communicator;

		this.idManager = new IDManager<>();

		this.running = new AtomicBoolean();
	}

	// Don't even try to catch IOExceptions; just crash.
	// Exercise has to handle this correctly anyway as this behaviour could also created maliciously.
	public void run() throws IOException
	{
		running.set(true);
		MultiplexedDataInputStream in0 = multiplexer.getIn(0);
		try
		{
			loop: for(;;)
			{
				switch(ThreadIndependentCommand.decode(in0.readByte()))
				{
					case NEW_THREAD -> respondNewThread(in0);
					case REF_DELETED -> respondRefDeleted(in0);
					case SHUTDOWN ->
					{
						break loop;
					}
				}
			}
			running.set(false);
			// notify exercise side we received the shutdown signal
			MultiplexedDataOutputStream out0 = multiplexer.getOut(0);
			out0.writeByte(SHUTDOWN_FINISHED.encode());
			out0.flush();
			multiplexer.close();
		} catch(RuntimeException e)
		{
			//TODO log to somewhere instead of rethrowing
			throw e;
		}
	}

	private void studentSideThread(DataInputStream in, DataOutputStream out)
	{
		try
		{
			for(;;)
			{
				switch(ThreadCommand.decode(in.readByte()))
				{
					case GET_CLASSNAME -> respondGetStudentSideClassname(in, out);
					case SEND -> respondSend(in, out);
					case RECEIVE -> respondReceive(in, out);
					case CALL_CONSTRUCTOR -> respondCallConstructor(in, out);
					case CALL_STATIC_METHOD -> respondCallStaticMethod(in, out);
					case GET_STATIC_FIELD -> respondGetStaticField(in, out);
					case SET_STATIC_FIELD -> respondSetStaticField(in, out);
					case CALL_INSTANCE_METHOD -> respondCallInstanceMethod(in, out);
					case GET_INSTANCE_FIELD -> respondGetInstanceField(in, out);
					case SET_INSTANCE_FIELD -> respondSetInstanceField(in, out);
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

	private void respondGetStudentSideClassname(DataInput in, DataOutput out) throws IOException
	{
		REF ref = readRef(in);

		out.writeUTF(communicator.getStudentSideClassname(ref));
	}

	private void respondCallConstructor(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		Args<REF> args = readArgs(in);

		writeRef(out, communicator.callConstructor(cn, args.params(), args.argRefs()));
	}

	private void respondCallStaticMethod(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String returnClassname = in.readUTF();
		Args<REF> args = readArgs(in);

		writeRef(out, communicator.callStaticMethod(cn, name, returnClassname, args.params(), args.argRefs()));
	}
	private void respondGetStaticField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();

		writeRef(out, communicator.getStaticField(cn, name, fieldClassname));
	}
	private void respondSetStaticField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		REF valueRef = readRef(in);

		communicator.setStaticField(cn, name, fieldClassname, valueRef);
	}

	private void respondCallInstanceMethod(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String returnClassname = in.readUTF();
		REF receiverRef = readRef(in);
		Args<REF> args = readArgs(in);

		writeRef(out, communicator.callInstanceMethod(cn, name, returnClassname, args.params(), receiverRef, args.argRefs()));
	}
	private void respondGetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		REF receiverRef = readRef(in);

		writeRef(out, communicator.getInstanceField(cn, name, fieldClassname, receiverRef));
	}
	private void respondSetInstanceField(DataInput in, DataOutput out) throws IOException
	{
		String cn = in.readUTF();
		String name = in.readUTF();
		String fieldClassname = in.readUTF();
		REF receiverRef = readRef(in);
		REF valueRef = readRef(in);

		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
	}

	private void respondSend(DataInput in, DataOutput out) throws IOException
	{
		REF serializerRef = readRef(in);
		int serializerInID = in.readInt();

		writeRef(out, communicator.send(serializerRef, multiplexer.getIn(serializerInID)));
	}

	private void respondReceive(DataInput in, DataOutputStream out) throws IOException
	{
		REF serializerRef = readRef(in);
		REF objRef = readRef(in);
		MultiplexedDataOutputStream serializerOut = multiplexer.getOut(in.readInt());

		out.writeByte(SERIALIZER_READY.encode());
		out.flush();
		communicator.receive(serializerRef, objRef, serializerOut);
		serializerOut.flush();
	}

	private void respondNewThread(MultiplexedDataInputStream in0) throws ClosedException, IOException
	{
		MultiplexedDataOutputStream out = multiplexer.getOut(in0.readInt());
		MultiplexedDataInputStream in = multiplexer.getIn(in0.readInt());

		new Thread(() -> studentSideThread(in, out)).start();
	}
	private void respondRefDeleted(DataInput in) throws IOException
	{
		REF deletedRef = readRef(in);
		int receivedCount = in.readInt();

		idManager.refDeleted(deletedRef, receivedCount);
	}

	private Args<REF> readArgs(DataInput in) throws IOException
	{
		int paramCount = in.readInt();

		List<String> params = new ArrayList<>(paramCount);
		for(int i = 0; i < paramCount; i ++)
			params.add(in.readUTF());

		List<REF> argRefs = new ArrayList<>(paramCount);
		for(int i = 0; i < paramCount; i ++)
			argRefs.add(readRef(in));

		return new Args<>(params, argRefs);
	}
	private record Args<REF> (List<String> params, List<REF> argRefs)
	{}

	protected final REF readRef(DataInput in) throws IOException
	{
		return idManager.getRef(in.readInt());
	}

	protected final void writeRef(DataOutput out, REF ref) throws IOException
	{
		out.writeInt(idManager.getIDForSending(ref));
	}
}
