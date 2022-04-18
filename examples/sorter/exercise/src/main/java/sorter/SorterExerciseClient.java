package sorter;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorClientSide.maybeWrapLoggingC;

import java.io.IOException;
import java.net.Socket;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;
import net.haspamelodica.studentcodeseparator.refs.Ref;

public class SorterExerciseClient
{
	private static final boolean LOGGING = false;

	public static void main(String[] args) throws IOException, InterruptedException
	{
		if(args.length != 2)
			throw new IllegalArgumentException("Usage: java " + SorterExerciseClient.class.getName() + " host port");
		// This code will eventually be moved to the framework.
		String host = args[0];
		int port;
		try
		{
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Port not a number: " + args[1]);
		}

		try(Socket sock = new Socket(host, port))
		{
			DataCommunicatorClient<Ref<Integer, Object>> client = new DataCommunicatorClient<>(
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
