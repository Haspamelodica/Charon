package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.impl.LoggingCommunicatorServerSide.maybeWrapLoggingS;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.ServerSocket;
import java.net.Socket;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorServerSide;
import net.haspamelodica.charon.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorServerSideSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorServerSideSupplierImpl;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicatorServerSide;

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
			StudentSideCommunicatorServerSide<?> directComm = new DirectSameJVMCommunicatorServerSide();
			RefTranslatorCommunicatorServerSideSupplier translatedComm = new RefTranslatorCommunicatorServerSideSupplierImpl<>(directComm);
			RefTranslatorCommunicatorServerSideSupplier loggingComm = maybeWrapLoggingS(translatedComm, LOGGING);
			DataCommunicatorServer server = new DataCommunicatorServer(sock.getInputStream(), sock.getOutputStream(), loggingComm);
			server.run();
		}

		Reference.reachabilityFence(softref);
	}
}
