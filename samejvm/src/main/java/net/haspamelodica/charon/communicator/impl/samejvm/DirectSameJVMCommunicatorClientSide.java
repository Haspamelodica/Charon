package net.haspamelodica.charon.communicator.impl.samejvm;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOBiConsumer;
import net.haspamelodica.charon.communicator.impl.data.exercise.IOFunction;
import net.haspamelodica.charon.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.charon.impl.StudentSideImpl;
import net.haspamelodica.charon.marshaling.SerDes;

/**
 * <b>Using this class in the tester JVM to create a {@link StudentSideImpl} is not safe
 * as it gives student code full access to the system the tester JVM is running on.</b>
 * Use a {@link DataCommunicatorClient} instead.
 * <p>
 * Commands are executed in the same JVM and same thread as they are called in; they also aren't serialized and deserialized.
 * This makes debugging a little bit easier and speeds up execution
 * compared to a {@link DataCommunicatorClient} and {@link DataCommunicatorServer} in the same JVM.
 */
//TODO better exception handling. Use StudentSideException
public class DirectSameJVMCommunicatorClientSide extends DirectSameJVMCommunicator implements StudentSideCommunicatorClientSide<Object>
{
	public DirectSameJVMCommunicatorClientSide(StudentSideCommunicatorCallbacks<Object> callbacks)
	{
		super(callbacks);
	}

	@Override
	public <T> Object send(Object serdesRef, IOBiConsumer<DataOutput, T> sendObj, T obj)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		SerDes<T> serdes = (SerDes<T>) serdesRef;
		return sendAndReceive(sendObj, serdes::deserialize, obj);
	}
	@Override
	public <T> T receive(Object serdesRef, IOFunction<DataInput, T> receiveObj, Object objRef)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		SerDes<T> serdes = (SerDes<T>) serdesRef;
		@SuppressWarnings("unchecked") // caller is responsible for this
		T obj = (T) objRef;
		return sendAndReceive(serdes::serialize, receiveObj, obj);
	}
	private <T> T sendAndReceive(IOBiConsumer<DataOutput, T> sender, IOFunction<DataInput, T> receiver, T obj)
	{
		// We have to actually serialize and deserialize the object to be compatible with a "real" communicator
		// in case the passed object is mutable or if any code relies on object identity.
		Thread serdesThread = null;
		AtomicReference<IOException> serializationIOExceptionA = new AtomicReference<>();
		AtomicReference<RuntimeException> serializationExceptionA = new AtomicReference<>();
		try(PipedInputStream pipeIn = new PipedInputStream(); DataInputStream in = new DataInputStream(pipeIn))
		{
			Semaphore pipeOutCreated = new Semaphore(0);
			// serialize in other thread to avoid blocking indefinitely if pipeOut buffer run out
			serdesThread = new Thread(() ->
			{
				try(PipedOutputStream pipeOut = new PipedOutputStream(pipeIn); DataOutputStream out = new DataOutputStream(pipeOut))
				{
					pipeOutCreated.release();
					sender.accept(out, obj);
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
			serdesThread.start();

			pipeOutCreated.acquireUninterruptibly();
			T result = receiver.apply(in);

			doUninterruptible(serdesThread::join);

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
			doUninterruptible(serdesThread::join);
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
