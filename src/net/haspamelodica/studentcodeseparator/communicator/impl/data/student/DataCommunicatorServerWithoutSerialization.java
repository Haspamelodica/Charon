package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.Command;

public abstract class DataCommunicatorServerWithoutSerialization<REF extends Ref<Integer>>
{
	private final DataInputStream	rawIn;
	private final DataOutputStream	rawOut;

	private final StudentSideCommunicatorWithoutSerialization<Integer, REF> communicator;

	private final IDManager<REF> idManager;

	public DataCommunicatorServerWithoutSerialization(DataInputStream rawIn, DataOutputStream rawOut,
			StudentSideCommunicatorWithoutSerialization<Integer, REF> communicator)
	{
		this.rawIn = rawIn;
		this.rawOut = rawOut;

		this.communicator = communicator;

		this.idManager = new IDManager<>();
	}

	public void run()
	{
		try
		{
			loop: for(;;)
			{
				switch(Command.decode(rawIn.readByte()))
				{
					case GET_CLASSNAME -> respondGetStudentSideClassname(rawIn, rawOut);
					case SEND -> respondSend(rawIn, rawOut);
					case RECEIVE -> respondReceive(rawIn, rawOut);
					case CALL_CONSTRUCTOR -> respondCallConstructor(rawIn, rawOut);
					case CALL_STATIC_METHOD -> respondCallStaticMethod(rawIn, rawOut);
					case GET_STATIC_FIELD -> respondGetStaticField(rawIn, rawOut);
					case SET_INSTANCE_FIELD -> respondSetInstanceField(rawIn, rawOut);
					case CALL_INSTANCE_METHOD -> respondCallInstanceMethod(rawIn, rawOut);
					case GET_INSTANCE_FIELD -> respondGetInstanceField(rawIn, rawOut);
					case SET_STATIC_FIELD -> respondSetStaticField(rawIn, rawOut);
					case SHUTDOWN ->
					{
						break loop;
					}
				}
				rawOut.flush();
			}
		} catch(Exception e)
		{
			//TODO log to rawOut
			throw new RuntimeException(e);
		}
	}

	private void respondGetStudentSideClassname(DataInput in, DataOutput out) throws IOException
	{
		REF ref = readRef(in);

		out.writeUTF(communicator.getStudentSideClassname(ref));
	}

	protected abstract void respondSend(DataInput in, DataOutput out) throws IOException;
	protected abstract void respondReceive(DataInput in, DataOutput out) throws IOException;

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
		out.writeInt(idManager.getID(ref));
	}
}
