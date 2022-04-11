package sorter;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorClientSide.maybeWrapLoggingC;

import java.io.IOException;
import java.net.Socket;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class SorterExerciseClient
{
	private static final boolean	LOGGING	= false;
	private static final String		HOST	= "localhost";
	private static final int		PORT	= 1337;

	public static void main(String[] args) throws IOException, InterruptedException
	{
		// This code will eventually be moved to the framework.
		try(Socket sock = new Socket(HOST, PORT))
		{
			DataCommunicatorClient<Ref<Integer, ?, Integer, StudentSideInstance, ?, ?>> client = new DataCommunicatorClient<>(
					sock.getInputStream(), sock.getOutputStream());
			try
			{
				SorterExercise.run(new StudentSideImpl<>(maybeWrapLoggingC(client, LOGGING)));
			} finally
			{
				client.shutdown();
			}
		}
	}
}
