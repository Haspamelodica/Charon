package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorClientSide.maybeWrapLoggingC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.utils.CommunicatingSideRunner;

public class ExerciseSideRunner
{
	public static void run(Consumer<StudentSide> exerciseSide, Class<?> mainClass, String... args)
			throws IOException, InterruptedException
	{
		CommunicatingSideRunner.run((in, out, logging) -> run(exerciseSide, in, out, logging), mainClass, args);
	}

	public static void run(Consumer<StudentSide> exerciseSide, InputStream in, OutputStream out, boolean logging)
	{
		DataCommunicatorClient<Ref<Integer, Object>> client = new DataCommunicatorClient<>(in, out);
		try
		{
			exerciseSide.accept(new StudentSideImpl<>(maybeWrapLoggingC(client, logging)));
		} finally
		{
			client.shutdown();
		}
	}
}
