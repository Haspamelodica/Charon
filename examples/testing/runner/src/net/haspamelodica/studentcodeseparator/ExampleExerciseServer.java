package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorWithoutSerialization.maybeWrapLoggingW;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.ServerSocket;
import java.net.Socket;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorWithoutSerialization;

//TODO this sometimes crashes
public class ExampleExerciseServer
{
	private static final boolean	LOGGING	= false;
	public static final int			PORT	= 1337;

	public static void main(String[] args) throws IOException
	{
		ReferenceQueue<Object> queue = new ReferenceQueue<>();
		SoftReference<Object> softref = new SoftReference<>(new char[500000000], queue);
		Thread softRefClearListener = new Thread(() ->
		{
			try
			{
				queue.remove();
				System.out.println("Soft ref got cleared");
			} catch(InterruptedException e)
			{}
		});
		softRefClearListener.setDaemon(true);
		softRefClearListener.start();

		try(ServerSocket serverSocket = new ServerSocket(PORT); Socket sock = serverSocket.accept())
		{
			DataCommunicatorServer server = new DataCommunicatorServer(sock.getInputStream(), sock.getOutputStream(),
					refManager -> maybeWrapLoggingW(new DirectSameJVMCommunicatorWithoutSerialization<>(refManager), LOGGING));
			server.run();
		}

		System.out.println(softref);
	}
}
