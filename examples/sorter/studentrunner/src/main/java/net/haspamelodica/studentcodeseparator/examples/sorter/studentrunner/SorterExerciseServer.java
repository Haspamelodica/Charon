package net.haspamelodica.studentcodeseparator.examples.sorter.studentrunner;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorServerSide.maybeWrapLoggingS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.refs.direct.WeakDirectRefManager;
import net.haspamelodica.studentcodeseparator.refs.intref.owner.IDReferrer;

public class SorterExerciseServer
{
	private static final boolean LOGGING = false;

	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("Usage: java " + SorterExerciseServer.class.getName() + " port");
		// This code will eventually be moved to the framework.
		int port;
		try
		{
			port = Integer.parseInt(args[0]);
		} catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Port not a number: " + args[1]);
		}

		try(ServerSocket serverSocket = new ServerSocket(port); Socket sock = serverSocket.accept())
		{
			DataCommunicatorServer<?> server = new DataCommunicatorServer<>(sock.getInputStream(), sock.getOutputStream(),
					maybeWrapLoggingS(new DirectSameJVMCommunicatorServerSide<>(new WeakDirectRefManager<
							Ref<Object, IDReferrer>>()), LOGGING));
			server.run();
		}
	}
}
