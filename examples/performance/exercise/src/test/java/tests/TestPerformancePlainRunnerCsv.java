package tests;

import java.io.IOException;

import net.haspamelodica.charon.CloseableDataCommStudentSide;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;
import perf.CallCounter;

public class TestPerformancePlainRunnerCsv
{
	public static void main(String[] args) throws IOException, InterruptedException, IncorrectUsageException
	{
		try(CloseableDataCommStudentSide studentSide = new CloseableDataCommStudentSide(args))
		{
			TestPerformance.outputAsCsv = true;
			TestPerformance testPerformance = new TestPerformance(studentSide.getStudentSide().createPrototype(CallCounter.Prototype.class));
			testPerformance.testPerformanceRegular();
			testPerformance.testPerformanceCallback();
			testPerformance.testPerformanceRegularParams();
			testPerformance.testPerformanceCallbackParams();
		}
	}
}
