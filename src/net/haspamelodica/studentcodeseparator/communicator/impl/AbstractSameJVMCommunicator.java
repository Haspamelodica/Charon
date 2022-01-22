package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import net.haspamelodica.studentcodeseparator.Serializer;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;

public abstract class AbstractSameJVMCommunicator implements StudentSideCommunicator<Object>
{
	@Override
	public <T> Object send(Serializer<T> serializer, Object serializerRef, T obj)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		Serializer<T> studentSideSerializer = (Serializer<T>) serializerRef;
		return sendAndReceive(serializer, studentSideSerializer, obj);
	}
	@Override
	public <T> T receive(Serializer<T> serializer, Object serializerRef, Object objRef)
	{
		@SuppressWarnings("unchecked") // caller is responsible for this
		Serializer<T> studentSideSerializer = (Serializer<T>) serializerRef;
		@SuppressWarnings("unchecked") // caller is responsible for this
		T obj = (T) objRef;
		return sendAndReceive(studentSideSerializer, serializer, obj);
	}
	private <T> T sendAndReceive(Serializer<T> serializer, Serializer<T> deserializer, T obj)
	{
		// We have to actually serialize and deserialize the object to be compatible with a "real" communicator
		// in case the passed object is mutable or if any code relies on object identity.
		Thread serializerThread = null;
		AtomicReference<IOException> serializationExceptionA = new AtomicReference<>();
		try(PipedInputStream pipeIn = new PipedInputStream(); DataInputStream in = new DataInputStream(pipeIn))
		{
			Semaphore pipeOutCreated = new Semaphore(0);
			//serialize in other thread to avoid deadlock
			serializerThread = new Thread(() ->
			{
				try(PipedOutputStream pipeOut = new PipedOutputStream(pipeIn); DataOutputStream out = new DataOutputStream(pipeOut))
				{
					pipeOutCreated.release();
					serializer.serialize(out, obj);
				} catch(IOException e)
				{
					serializationExceptionA.set(e);
				} finally
				{
					//maybe releases twice. That doesn't matter.
					pipeOutCreated.release();
				}
			});
			serializerThread.start();

			pipeOutCreated.acquireUninterruptibly();
			T result = deserializer.deserialize(in);

			doUninterruptible(serializerThread::join);
			IOException serializationException = serializationExceptionA.get();
			if(serializationException != null)
				throw new UncheckedIOException(serializationException);

			return result;
		} catch(IOException e)
		{
			doUninterruptible(serializerThread::join);
			IOException serializationException = serializationExceptionA.get();
			if(serializationException != null)
				e.addSuppressed(serializationException);
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
