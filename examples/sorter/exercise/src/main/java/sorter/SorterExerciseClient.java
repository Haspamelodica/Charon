package sorter;

import java.io.IOException;

import net.haspamelodica.studentcodeseparator.ExerciseSideRunner;

public class SorterExerciseClient
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		ExerciseSideRunner.run(SorterExercise::run, SorterExerciseClient.class, args);
	}
}
