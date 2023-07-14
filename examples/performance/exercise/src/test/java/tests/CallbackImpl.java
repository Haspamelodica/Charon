package tests;

import net.haspamelodica.charon.annotations.SafeForCallByStudent;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;
import perf.CallbackInterface;

public class CallbackImpl implements CallbackInterface
{
	private int callCount;

	@Override
	@SafeForCallByStudent
	public void call()
	{
		callCount ++;
	}

	@Override
	@SafeForCallByStudent
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

	// If we don't mark toString as callable by student,
	// debugging the student submission becomes hard because
	// many debuggers automatically call toString on relevant objects.
	@Override
	@SafeForCallByStudent
	@UseSerDes(StringSerDes.class)
	public String toString()
	{
		return "MazeSolution";
	}
}
