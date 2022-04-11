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
	private static final boolean	LOGGING	= false;
	public static final int			PORT	= 1337;

	public static void main(String[] args) throws IOException
	{
		try(ServerSocket serverSocket = new ServerSocket(PORT); Socket sock = serverSocket.accept())
		{
			DataCommunicatorServer<?> server = new DataCommunicatorServer<>(sock.getInputStream(), sock.getOutputStream(),
					maybeWrapLoggingS(new DirectSameJVMCommunicatorServerSide<>(new WeakDirectRefManager<
							Ref<Object, IDReferrer, Object, ?, ?, ?>>()), LOGGING));
			server.run();
		}
	}
}
