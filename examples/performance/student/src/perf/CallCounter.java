package perf;

public class CallCounter
{
	private int callCount;

	public void call()
	{
		callCount ++;
	}

	public void call(
			long unusedParam00, long unusedParam01, long unusedParam02, long unusedParam03,
			long unusedParam04, long unusedParam05, long unusedParam06, long unusedParam07,
			long unusedParam08, long unusedParam09, long unusedParam10, long unusedParam11,
			long unusedParam12, long unusedParam13, long unusedParam14, long unusedParam15)
	{
		callCount ++;
	}

	public int getCallCount()
	{
		return callCount;
	}

	public static void callForNSeconds(CallbackInterface callback, double secondsToRunFor)
	{
		long start = System.nanoTime();
		long nanosToRunFor = (long) (secondsToRunFor * 1000 * 1000 * 1000);
		while(System.nanoTime() - start < nanosToRunFor)
			callback.call();
	}

	public static void callForNSecondsWithUnchangingUnusedParams(CallbackInterface callback, double secondsToRunFor)
	{
		long start = System.nanoTime();
		long nanosToRunFor = (long) (secondsToRunFor * 1000 * 1000 * 1000);
		while(System.nanoTime() - start < nanosToRunFor)
			callback.call(
					42, 1337, 1, 2,
					0, -1, Long.MAX_VALUE, Long.MIN_VALUE,
					1234, 4321, 5678, 8765,
					987654321, 123456789, -987654321, -123456789);
	}
}
