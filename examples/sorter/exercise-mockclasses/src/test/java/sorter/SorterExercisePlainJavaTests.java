package sorter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Usage: Either use {@link SorterExercisePlainJavaManualRunner},
 * or run this class directly with the following arguments:
 * 
 * <pre>
 * -Djava.system.class.loader=net.haspamelodica.charon.mockclasses.CharonSystemClassloader
 * -Dnet.haspamelodica.charon.templatecode.classes=sorter.Sorter
 * -Dnet.haspamelodica.charon.communicationargs="socket localhost 1337"
 * </pre>
 * 
 * In either case, run the student side first.
 */
public class SorterExercisePlainJavaTests
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	public static void main(String[] args)
	{
		StringArrayList list = new StringArrayList(STRINGS.length);

		list.add(STRINGS[0]);
		list.add(STRINGS[1]);
		for(int i = 2; i < STRINGS.length; i ++)
			list.add(STRINGS[i]);

		checkListElements(STRINGS, list);

		StringArrayList sortedList = Sorter.sort(list);

		checkListElements(STRINGS, list);
		String[] sortedStrings = Arrays.copyOf(STRINGS, STRINGS.length);
		Arrays.sort(sortedStrings);
		checkListElements(sortedStrings, sortedList);

		System.out.println("All tests successful!");
	}

	private static void checkListElements(String[] expected, StringArrayList actualList)
	{
		for(int i = 0; i < expected.length; i ++)
			assertEquals("index " + i, expected[i], actualList.get(i));
	}

	// A real exercise would probably use a proper testing framework like JUnit.
	// Unfortunately, JUnit so far doesn't integrate well.
	private static <T> void assertEquals(String context, T expected, T actual)
	{
		if(!Objects.equals(expected, actual))
			throw new RuntimeException((context != null ? context + ": " : "") + "Expected: " + expected + ", actual " + actual);
	}
}
