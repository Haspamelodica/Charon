package tests;

import java.io.IOException;

import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class TestPerformancePlainRunnerFastCsv
{
	public static void main(String[] args) throws IOException, InterruptedException, IncorrectUsageException
	{
		TestPerformance.WARMUP_RUNTIME_SECONDS = 2;
		TestPerformance.RUNTIME_SECONDS = 2;
		TestPerformance.OUTPUT_AS_CSV = true;
		TestPerformancePlainRunner.main(args);
	}
}
