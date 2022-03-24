package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.student.DataCommunicatorServerWithoutSerialization;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;
import net.haspamelodica.studentcodeseparator.refs.DirectRef;
import net.haspamelodica.studentcodeseparator.refs.DirectRefManager;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

/**
 * <b>Using this class in the tester JVM to create a {@link StudentSideImpl} is not safe
 * as it gives student code full access to the system the tester JVM is running on.</b>
 * Use a {@link DataCommunicatorClient} instead.
 * <p>
 * Commands are executed in the same JVM and same thread as they are called in; they also aren't serialized and deserialized.
 * This makes debugging a little bit easier and speeds up execution
 * compared to a {@link DataCommunicatorClient} and {@link DataCommunicatorServerWithoutSerialization} in the same JVM.
 */
//TODO better exception handling. Use StudentSideException
public class DirectSameJVMCommunicator<ATTACHMENT> extends DirectSameJVMCommunicatorWithoutSerialization<ATTACHMENT>
		implements StudentSideCommunicator<ATTACHMENT, DirectRef<ATTACHMENT>>
{
	public DirectSameJVMCommunicator(DirectRefManager<ATTACHMENT> refManager)
	{
		super(refManager);
	}

	@Override
	public <T> DirectRef<ATTACHMENT> send(Serializer<T> serializer, DirectRef<ATTACHMENT> serializerRef, T obj)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		Serializer<T> studentSideSerializer = (Serializer<T>) refManager.unpack(serializerRef);
		return refManager.pack(sendAndReceive(serializer, studentSideSerializer, obj));
	}
	@Override
	public <T> T receive(Serializer<T> serializer, DirectRef<ATTACHMENT> serializerRef, DirectRef<ATTACHMENT> objRef)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		Serializer<T> studentSideSerializer = (Serializer<T>) refManager.unpack(serializerRef);
		@SuppressWarnings("unchecked") // caller is responsible for this
		T obj = (T) refManager.unpack(objRef);
		return sendAndReceive(studentSideSerializer, serializer, obj);
	}
	private <T> T sendAndReceive(Serializer<T> serializer, Serializer<T> deserializer, T obj)
	{
		// We have to actually serialize and deserialize the object to be compatible with a "real" communicator
		// in case the passed object is mutable or if any code relies on object identity.
		Thread serializerThread = null;
		AtomicReference<IOException> serializationIOExceptionA = new AtomicReference<>();
		AtomicReference<RuntimeException> serializationExceptionA = new AtomicReference<>();
		try(PipedInputStream pipeIn = new PipedInputStream(); DataInputStream in = new DataInputStream(pipeIn))
		{
			Semaphore pipeOutCreated = new Semaphore(0);
			// serialize in other thread to avoid blocking indefinitely if pipeOut buffer run out
			serializerThread = new Thread(() ->
			{
				try(PipedOutputStream pipeOut = new PipedOutputStream(pipeIn); DataOutputStream out = new DataOutputStream(pipeOut))
				{
					pipeOutCreated.release();
					serializer.serialize(out, obj);
				} catch(IOException e)
				{
					serializationIOExceptionA.set(e);
				} catch(RuntimeException e)
				{
					serializationExceptionA.set(e);
				} finally
				{
					// maybe releases twice. That doesn't matter.
					pipeOutCreated.release();
				}
			});
			serializerThread.start();

			pipeOutCreated.acquireUninterruptibly();
			T result = deserializer.deserialize(in);

			doUninterruptible(serializerThread::join);

			IOException serializationIOException = serializationIOExceptionA.get();
			RuntimeException serializationException = serializationExceptionA.get();
			if(serializationException != null)
			{
				if(serializationIOException != null)
					serializationException.addSuppressed(serializationIOException);
				throw serializationException;
			}
			if(serializationIOException != null)
				throw serializationIOException;

			return result;
		} catch(IOException e)
		{
			doUninterruptible(serializerThread::join);
			IOException serializationIOException = serializationIOExceptionA.get();
			RuntimeException serializationException = serializationExceptionA.get();
			if(serializationException != null)
			{
				serializationException.addSuppressed(e);
				if(serializationIOException != null)
					serializationException.addSuppressed(serializationIOException);
				throw serializationException;
			}
			if(serializationIOException != null)
				e.addSuppressed(serializationIOException);
			throw new UncheckedIOException(e);
		}
	}
	private void doUninterruptible(InterruptibleRunnable action)
	{
		boolean interrupted = false;
		for(;;)
			try
			{
				action.run();
				break;
			} catch(InterruptedException e)
			{
				interrupted = true;
			}
		if(interrupted)
			Thread.currentThread().interrupt();
	}

	private static interface InterruptibleRunnable
	{
		public void run() throws InterruptedException;
	}
}
