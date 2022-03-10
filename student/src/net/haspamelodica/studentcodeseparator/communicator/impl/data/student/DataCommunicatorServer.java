package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import static net.haspamelodica.studentcodeseparator.communicator.impl.data.ThreadResponse.SERIALIZER_READY;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import net.haspamelodica.streammultiplexer.MultiplexedDataOutputStream;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorWithoutSerialization;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRef;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.SameJVMRefManager;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.WeakSameJVMRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

// TODO server, client or both crash on shutdown
public class DataCommunicatorServer extends DataCommunicatorServerWithoutSerialization<SameJVMRef<DataCommunicatorAttachment>>
{
	private final SameJVMRefManager<DataCommunicatorAttachment> refManager;

	public DataCommunicatorServer(InputStream rawIn, OutputStream rawOut)
	{
		this(rawIn, rawOut, DirectSameJVMCommunicatorWithoutSerialization::new);
	}
	/**
	 * This constructor exists so {@link LoggingCommunicatorWithoutSerialization} can be used server-side.
	 */
	public DataCommunicatorServer(InputStream rawIn, OutputStream rawOut,
			Function<SameJVMRefManager<DataCommunicatorAttachment>, StudentSideCommunicatorWithoutSerialization<DataCommunicatorAttachment,
					SameJVMRef<DataCommunicatorAttachment>>> createCommunicator)
	{
		this(rawIn, rawOut, createCommunicator, new WeakSameJVMRefManager<>());
	}
	// extracted into own constructor so we can use refManager in super constructor call and store it as a final field
	private DataCommunicatorServer(InputStream rawIn, OutputStream rawOut,
			Function<SameJVMRefManager<DataCommunicatorAttachment>, StudentSideCommunicatorWithoutSerialization<DataCommunicatorAttachment,
					SameJVMRef<DataCommunicatorAttachment>>> createCommunicator,
			SameJVMRefManager<DataCommunicatorAttachment> refManager)
	{
		super(rawIn, rawOut, createCommunicator.apply(refManager));
		this.refManager = refManager;
	}

	@Override
	protected void respondSend(DataInput in, DataOutput out) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(readRef(in));
		int serializerInID = in.readInt();

		Object result = serializer.deserialize(multiplexer.getIn(serializerInID));

		writeRef(out, refManager.pack(result));
	}

	@Override
	protected void respondReceive(DataInput in, DataOutputStream out) throws IOException
	{
		Serializer<?> serializer = (Serializer<?>) refManager.unpack(readRef(in));
		Object obj = refManager.unpack(readRef(in));
		MultiplexedDataOutputStream serializerOut = multiplexer.getOut(in.readInt());

		out.writeByte(SERIALIZER_READY.encode());
		out.flush();
		respondReceive(serializerOut, serializer, obj);
		serializerOut.flush();
	}

	// extracted to own method so cast to T is expressible in Java
	private <T> void respondReceive(DataOutput out, Serializer<T> serializer, Object obj) throws IOException
	{
		@SuppressWarnings("unchecked") // responsibility of server
		T objCasted = (T) obj;
		serializer.serialize(out, objCasted);
	}
}
