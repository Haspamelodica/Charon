package sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Usage: Run this class as a JUnit test with the following arguments:
 * 
 * <pre>
 * -Djava.system.class.loader=net.haspamelodica.charon.mockclasses.CharonSystemClassloader
 * -Dnet.haspamelodica.charon.templatecode.classes=sorter.Sorter
 * -Dnet.haspamelodica.charon.communicationargs="socket localhost 1337"
 * </pre>
 * 
 * Run the student side first.
 */
public class SorterTest
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	@Test
	public void testSort()
	{
		StringArrayList list = new StringArrayList(STRINGS.length);
		for(String string : STRINGS)
			list.add(string);

		StringArrayList sortedList = Sorter.sort(list);

		checkListElements(STRINGS, list);

		String[] sortedStrings = Arrays.copyOf(STRINGS, STRINGS.length);
		Arrays.sort(sortedStrings);
		checkListElements(sortedStrings, sortedList);
	}

	private static void checkListElements(String[] expected, StringArrayList actualList)
	{
		for(int i = 0; i < expected.length; i ++)
			assertEquals(expected[i], actualList.get(i), "index " + i + " mismatched");
	}
}
