package sorter;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicator.maybeWrapLogging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;

public class SorterExerciseClient
{
	private static final boolean	LOGGING	= false;
	private static final String		HOST	= "localhost";
	private static final int		PORT	= 1337;

	public static void main(String[] args) throws IOException, InterruptedException
	{
		// This code will eventually be moved to the framework.
		try(Socket sock = new Socket(HOST, PORT);
				DataInputStream in = new DataInputStream(sock.getInputStream());
				DataOutputStream out = new DataOutputStream(sock.getOutputStream()))
		{
			DataCommunicatorClient<StudentSideInstance> client = new DataCommunicatorClient<>(in, out);
			try
			{
				SorterExercise.run(new StudentSideImpl<>(maybeWrapLogging(client, LOGGING)));
			} finally
			{
				client.shutdown();
			}
		}
	}
}
