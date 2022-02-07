package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.ManualSameJVMRefManager;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRef;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class DataCommunicatorServer extends DataCommunicatorServerWithoutSerialization<SameJVMRef<Integer>>
{
	private final ManualSameJVMRefManager<Integer> refManager;

	public DataCommunicatorServer(DataInputStream rawIn, DataOutputStream rawOut)
	{
		this(rawIn, rawOut, DirectSameJVMCommunicatorWithoutSerialization::new);
	}
	/**
	 * This constructor exists so {@link LoggingCommunicatorWithoutSerialization} can be used server-side.
	 */
	public DataCommunicatorServer(DataInputStream rawIn, DataOutputStream rawOut,
			Function<SameJVMRefManager<Integer>, StudentSideCommunicatorWithoutSerialization<Integer, SameJVMRef<Integer>>> createCommunicator)
	{
		this(rawIn, rawOut, createCommunicator, new ManualSameJVMRefManager<>());
	}
	// extracted into own constructor so we can use refManager in super constructor call and store it as a final field
	private DataCommunicatorServer(DataInputStream rawIn, DataOutputStream rawOut,
			Function<SameJVMRefManager<Integer>, StudentSideCommunicatorWithoutSerialization<Integer, SameJVMRef<Integer>>> createCommunicator,
			ManualSameJVMRefManager<Integer> refManager)
	{
		super(rawIn, rawOut, createCommunicator.apply(refManager));
		this.refManager = refManager;
	}

	public void respondSend(DataInput in, DataOutput out) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(readRef(in));
		Object result = serializer.deserialize(in);

		writeRef(out, refManager.pack(result));
	}

	public void respondReceive(DataInput in, DataOutput out) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(readRef(in));
		Object obj = refManager.unpack(readRef(in));

		respondReceive(out, serializer, obj);
	}

	// extracted to own method so cast to T is expressible in Java
	private <T> void respondReceive(DataOutput out, Serializer<T> serializer, Object obj) throws IOException
	{
		@SuppressWarnings("unchecked") // responsibility of server
		T objCasted = (T) obj;
		serializer.serialize(out, objCasted);
	}
}
