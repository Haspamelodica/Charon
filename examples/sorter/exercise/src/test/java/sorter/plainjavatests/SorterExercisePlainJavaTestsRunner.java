package sorter.plainjavatests;

import java.io.IOException;

import net.haspamelodica.charon.WrappedStudentSide;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class SorterExercisePlainJavaTestsRunner
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		try(WrappedStudentSide exerciseSide = new WrappedStudentSide(args))
		{
			SorterExercisePlainJavaTests.run(exerciseSide.getStudentSide());
		} catch(IncorrectUsageException e)
		{
			e.printStackTrace();
			System.err.println("Usage: java " + SorterExercisePlainJavaTestsRunner.class.getName() + "  " + CommunicationArgsParser.argsSyntax());
		}
	}
}
