package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorClientSide.maybeWrapLoggingC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.studentcodeseparator.impl.StudentSideImpl;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.utils.CommunicatingSideRunner;

public class ExerciseSideRunner
{
	public static <X extends Throwable> void run(ThrowingConsumer<StudentSide, X> exerciseSide, Class<?> mainClass, String... args)
			throws IOException, InterruptedException, X
	{
		CommunicatingSideRunner.run((in, out, logging) -> run(exerciseSide, in, out, logging), mainClass, args);
	}

	public static <X extends Throwable> void run(ThrowingConsumer<StudentSide, X> exerciseSide,
			InputStream in, OutputStream out, boolean logging) throws X
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

	public static interface ThrowingConsumer<T, X extends Throwable>
	{
		public void accept(T t) throws X;
	}
}
