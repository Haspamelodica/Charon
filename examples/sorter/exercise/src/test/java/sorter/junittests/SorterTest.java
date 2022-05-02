package sorter.junittests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.junitextension.StudentCodeSeparatorExtension;
import sorter.Sorter;
import sorter.StringArrayList;

@ExtendWith(StudentCodeSeparatorExtension.class)
public class SorterTest
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	@Test
	public void testSort(StudentSide studentSide)
	{
		StringArrayList.Prototype StringArrayListP = studentSide.createPrototype(StringArrayList.Prototype.class);
		Sorter.Prototype SorterP = studentSide.createPrototype(Sorter.Prototype.class);

		StringArrayList list = StringArrayListP.new_(STRINGS.length);
		for(String string : STRINGS)
			list.add(string);

		StringArrayList sortedList = SorterP.sort(list);

		checkListElements(STRINGS, list);

		String[] sortedStrings = Arrays.copyOf(STRINGS, STRINGS.length);
		Arrays.sort(sortedStrings);
		checkListElements(sortedStrings, sortedList);
	}

	private static void checkListElements(String[] expected, StringArrayList actualList)
	{
		assertEquals(expected.length, actualList.length(), "list length mismatched");
		for(int i = 0; i < expected.length; i ++)
			assertEquals(expected[i], actualList.get(i), "index " + i + " mismatched");
	}
}
