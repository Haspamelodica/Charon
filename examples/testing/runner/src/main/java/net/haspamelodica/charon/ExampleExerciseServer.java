package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.impl.LoggingCommunicatorServerSide.maybeWrapLoggingS;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.ServerSocket;
import java.net.Socket;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.RefTranslatorCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicatorServerSide;
import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;

// TODO this sometimes crashes
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
			StudentSideCommunicatorServerSide<Object> directComm = new DirectSameJVMCommunicatorServerSide();
			StudentSideCommunicatorServerSide<LongRef> translatedComm = new RefTranslatorCommunicatorServerSide<>(directComm, LOGGING);
			StudentSideCommunicatorServerSide<LongRef> loggingComm = maybeWrapLoggingS(translatedComm, false);
			DataCommunicatorServer server = new DataCommunicatorServer(sock.getInputStream(), sock.getOutputStream(), loggingComm);
			server.run();
		}

		Reference.reachabilityFence(softref);
	}
}
