package sorter;

import static net.haspamelodica.charon.mockclasses.MockclassesUtils.classpathUrlForClass;

import java.io.IOException;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.ClasspathBasedDynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.WrappedMockclassStudentSide;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class SorterExercisePlainJavaManualRunner
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		DynamicInterfaceProvider interfaceProvider = new ClasspathBasedDynamicInterfaceProvider(classpathUrlForClass(Sorter.class));
		try(WrappedMockclassStudentSide wrappedStudentSide = new WrappedMockclassStudentSide(SorterExercisePlainJavaManualRunner.class.getClassLoader(),
				interfaceProvider, args, RunnerBodyInterface.class))
		{
			MockclassStudentSide studentSide = wrappedStudentSide.getStudentSide();

			RunnerBodyInterface runnerBody = studentSide.createInstanceWithMockclasses(RunnerBodyInterface.class, RunnerBodyImpl.class);
			runnerBody.run(args);

		} catch(IncorrectUsageException e)
		{
			e.printStackTrace();
			System.err.println("Usage: java " + SorterExercisePlainJavaManualRunner.class.getName() + "  " + CommunicationArgsParser.argsSyntax());
		}
	}

	// needs to be public because it needs to be visible across ClassLoaders
	public static class RunnerBodyImpl implements RunnerBodyInterface
	{
		@Override
		public void run(String[] args)
		{
			SorterExercisePlainJavaTests.main(args);
		}
	}
	// needs to be public because it needs to be visible across ClassLoaders
	public static interface RunnerBodyInterface
	{
		public void run(String[] args);
	}
}
