package net.haspamelodica.charon;

import static net.haspamelodica.charon.ExampleExercise.run;
import static net.haspamelodica.charon.communicator.ClientSideCommunicatorUtils.maybeWrapLoggingIntClient;
import static net.haspamelodica.charon.communicator.ClientSideSameJVMCommunicatorUtils.createDirectCommClient;
import static net.haspamelodica.charon.communicator.ServerSideCommunicatorUtils.createDirectCommServer;
import static net.haspamelodica.charon.communicator.ServerSideCommunicatorUtils.maybeWrapLoggingExtServer;
import static net.haspamelodica.charon.communicator.ServerSideCommunicatorUtils.wrapReftransExtServer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import net.haspamelodica.charon.communicator.impl.data.exercise.UninitializedDataCommunicatorClient;
import net.haspamelodica.charon.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLogger;
import net.haspamelodica.charon.impl.StudentSideImpl;

public class ExampleExerciseClient
{
	private static final boolean LOGGING = false;
	// If you use DATA_OTHER_JVM, start ExampleExerciseServer first.
	private static final Mode MODE = Mode.DATA_OTHER_JVM;
	// HOST and PORT only matter for mode DATA_OTHER_JVM
	private static final String	HOST	= "localhost";
	private static final int	PORT	= 1337;

	private enum Mode
	{
		DIRECT, DATA_SAME_JVM, DATA_OTHER_JVM;
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		// An instance of StudentSide will at some point be provided by the framework, not created by the exercise.
		// Also, Charon would not use a DirectSameJVMCommunicator (or DataCommunicatorServer) in the exercise JVM.
		switch(MODE)
		{
			case DIRECT -> runDirect();
			case DATA_SAME_JVM -> runDataSameJVM();
			case DATA_OTHER_JVM -> runDataOtherJVM();
		}
	}

	// --- Code below here will be moved to the framework.

	private static void runDirect()
	{
		run(new StudentSideImpl<>(
				maybeWrapLoggingIntClient(LOGGING, new CommunicationLogger(),
						createDirectCommClient())));
	}

	private static void runDataSameJVM() throws InterruptedException, IOException
	{
		try(PipedInputStream clientIn = new PipedInputStream(); PipedOutputStream clientOut = new PipedOutputStream())
		{
			Semaphore serverConnected = new Semaphore(0);
			new Thread(() ->
			{
				try(PipedInputStream serverIn = new PipedInputStream(clientOut); PipedOutputStream serverOut = new PipedOutputStream(clientIn))
				{
					serverConnected.release();
					DataCommunicatorServer server = new DataCommunicatorServer(serverIn, serverOut,
							maybeWrapLoggingExtServer(LOGGING, new CommunicationLogger("SERVER: "),
									wrapReftransExtServer(
											createDirectCommServer())));
					server.run();
				} catch(IOException e)
				{
					throw new UncheckedIOException(e);
				} finally
				{
					// Might release twice; doesn't matter
					serverConnected.release();
				}
			}).start();
			// wait for the server to create PipedOutputStreams
			serverConnected.acquire();
			UninitializedDataCommunicatorClient client = new UninitializedDataCommunicatorClient(clientIn, clientOut);
			run(new StudentSideImpl<>(maybeWrapLoggingIntClient(LOGGING, new CommunicationLogger("CLIENT: "), client)));
			client.shutdown();
		}
	}

	private static void runDataOtherJVM() throws IOException, UnknownHostException
	{
		try(Socket sock = new Socket(HOST, PORT))
		{
			UninitializedDataCommunicatorClient client = new UninitializedDataCommunicatorClient(
					sock.getInputStream(), sock.getOutputStream());
			try
			{
				run(new StudentSideImpl<>(maybeWrapLoggingIntClient(LOGGING, new CommunicationLogger(), client)));
			} finally
			{
				client.shutdown();
			}
		}
	}
}
