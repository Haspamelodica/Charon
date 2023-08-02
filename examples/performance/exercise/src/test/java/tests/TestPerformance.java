package tests;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import net.haspamelodica.charon.junitextension.CharonExtension;
import perf.CallCounter;

@ExtendWith(CharonExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class TestPerformance
{
	private static final double	WARMUP_RUNTIME_SECONDS	= 1;
	private static final double	RUNTIME_SECONDS			= 1;

	private final CallCounter.Prototype CallCounter;

	public TestPerformance(CallCounter.Prototype CallCounter)
	{
		this.CallCounter = CallCounter;
	}

	@Test
	@Order(1)
	public void testPerformanceRegular()
	{
		{
			CallCounter counter = CallCounter.new_();
			long start = System.nanoTime();
			long nanosToRunFor = (long) (WARMUP_RUNTIME_SECONDS * 1000 * 1000 * 1000);
			while(System.nanoTime() - start < nanosToRunFor)
				counter.call();
		}
		{
			CallCounter counter = CallCounter.new_();
			long start = System.nanoTime();
			long nanosToRunFor = (long) (RUNTIME_SECONDS * 1000 * 1000 * 1000);
			while(System.nanoTime() - start < nanosToRunFor)
				counter.call();
			System.out.println("Regular   : " + counter.getCallCount() / RUNTIME_SECONDS + " calls per second");
		}
	}

	@Test
	@Order(2)
	public void testPerformanceCallback()
	{
		{
			CallbackImpl callback = new CallbackImpl();
			CallCounter.callForNSeconds(callback, WARMUP_RUNTIME_SECONDS);
		}
		{
			CallbackImpl callback = new CallbackImpl();
			CallCounter.callForNSeconds(callback, RUNTIME_SECONDS);
			System.out.println("Callback  : " + callback.getCallCount() / RUNTIME_SECONDS + " calls per second");
		}
	}

	@Test
	@Order(3)
	public void testPerformanceRegularParams()
	{
		{
			CallCounter counter = CallCounter.new_();
			long start = System.nanoTime();
			long nanosToRunFor = (long) (WARMUP_RUNTIME_SECONDS * 1000 * 1000 * 1000);
			while(System.nanoTime() - start < nanosToRunFor)
				counter.call(
						42, 1337, 1, 2,
						0, -1, Long.MAX_VALUE, Long.MIN_VALUE,
						1234, 4321, 5678, 8765,
						987654321, 123456789, -987654321, -123456789);
		}
		{
			CallCounter counter = CallCounter.new_();
			long start = System.nanoTime();
			long nanosToRunFor = (long) (RUNTIME_SECONDS * 1000 * 1000 * 1000);
			while(System.nanoTime() - start < nanosToRunFor)
				counter.call(
						42, 1337, 1, 2,
						0, -1, Long.MAX_VALUE, Long.MIN_VALUE,
						1234, 4321, 5678, 8765,
						987654321, 123456789, -987654321, -123456789);
			System.out.println("Regular p : " + counter.getCallCount() / RUNTIME_SECONDS + " calls per second");
		}
	}

	@Test
	@Order(4)
	public void testPerformanceCallbackParams()
	{
		{
			CallbackImpl callback = new CallbackImpl();
			CallCounter.callForNSecondsWithUnchangingUnusedParams(callback, WARMUP_RUNTIME_SECONDS);
		}
		{
			CallbackImpl callback = new CallbackImpl();
			CallCounter.callForNSecondsWithUnchangingUnusedParams(callback, RUNTIME_SECONDS);
			System.out.println("Callback p: " + callback.getCallCount() / RUNTIME_SECONDS + " calls per second");
		}
	}
}
