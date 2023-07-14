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
	private final CallCounter.Prototype CallCounter;

	public TestPerformance(CallCounter.Prototype CallCounter)
	{
		this.CallCounter = CallCounter;
	}

	@Test
	@Order(1)
	public void warmup()
	{
		double secondsToRunFor = 1;

		CallCounter counter = CallCounter.new_();
		long start = System.nanoTime();
		long nanosToRunFor = (long) (secondsToRunFor * 1000 * 1000 * 1000);
		while(System.nanoTime() - start < nanosToRunFor)
			counter.call();

		start = System.nanoTime();
		while(System.nanoTime() - start < nanosToRunFor)
			counter.call(
					42, 1337, 1, 2,
					0, -1, Long.MAX_VALUE, Long.MIN_VALUE,
					1234, 4321, 5678, 8765,
					987654321, 123456789, -987654321, -123456789);

		CallCounter.callForNSeconds(new CallbackImpl(), secondsToRunFor);
		CallCounter.callForNSecondsWithUnchangingUnusedParams(new CallbackImpl(), secondsToRunFor);
	}

	@Test
	@Order(2)
	public void testPerformanceRegular()
	{
		double secondsToRunFor = 1;

		CallCounter counter = CallCounter.new_();
		long start = System.nanoTime();
		long nanosToRunFor = (long) (secondsToRunFor * 1000 * 1000 * 1000);
		while(System.nanoTime() - start < nanosToRunFor)
			counter.call();
		System.out.println("Regular   : " + counter.getCallCount() / secondsToRunFor + " calls per second");
	}

	@Test
	@Order(3)
	public void testPerformanceCallback()
	{
		double secondsToRunFor = 1;

		CallbackImpl callback = new CallbackImpl();
		CallCounter.callForNSeconds(callback, secondsToRunFor);
		System.out.println("Callback  : " + callback.getCallCount() / secondsToRunFor + " calls per second");
	}

	@Test
	@Order(4)
	public void testPerformanceRegularParams()
	{
		double secondsToRunFor = 1;

		CallCounter counter = CallCounter.new_();
		long start = System.nanoTime();
		long nanosToRunFor = (long) (secondsToRunFor * 1000 * 1000 * 1000);
		while(System.nanoTime() - start < nanosToRunFor)
			counter.call(
					42, 1337, 1, 2,
					0, -1, Long.MAX_VALUE, Long.MIN_VALUE,
					1234, 4321, 5678, 8765,
					987654321, 123456789, -987654321, -123456789);
		System.out.println("Regular p : " + counter.getCallCount() / secondsToRunFor + " calls per second");
	}

	@Test
	@Order(5)
	public void testPerformanceCallbackParams()
	{
		double secondsToRunFor = 1;

		CallbackImpl callback = new CallbackImpl();
		CallCounter.callForNSecondsWithUnchangingUnusedParams(callback, secondsToRunFor);
		System.out.println("Callback p: " + callback.getCallCount() / secondsToRunFor + " calls per second");
	}
}
