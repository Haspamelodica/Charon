package sorter.plainjavatests;

import java.util.Arrays;
import java.util.Objects;

import net.haspamelodica.charon.StudentSide;
import sorter.Sorter;
import sorter.StringArrayList;

public class SorterExercisePlainJavaTests
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	// To run, use SorterExercisePlainJavaTestsRunner, but start StudentSideRunner in examples.sorter.studentrunner first.
	public static void run(StudentSide studentSide)
	{
		// The framework provides an instance of StudentSide.
		// The StudentSide can (only) be used to obtain instances (implementations) of Prototypes.
		// Prototypes provide access to everything static of a class:
		// constructors, static fields, static methods.
		StringArrayList.Prototype StringArrayListP = studentSide.createPrototype(StringArrayList.Prototype.class);
		Sorter.Prototype SorterP = studentSide.createPrototype(Sorter.Prototype.class);

		// A prototype can, among other things, be used to create instances of student-side instances (SSIs).
		// On the student side, the constructor corresponding to the given parameters is called.
		StringArrayList list = StringArrayListP.new_(STRINGS.length);

		// An SSI can be used to call instance methods, to set instance fields, and to read instance fields.
		// reading an instance field:
		assertEquals(0, list.length());
		// calling an instance method:
		list.add(STRINGS[0]);
		assertEquals(1, list.length());
		list.add(STRINGS[1]);
		assertEquals(2, list.length());
		for(int i = 2; i < STRINGS.length; i ++)
			list.add(STRINGS[i]);

		checkListElements(STRINGS, list);

		// A prototype can also be used to call static methods.
		StringArrayList sortedList = SorterP.sort(list);

		checkListElements(STRINGS, list);
		String[] sortedStrings = Arrays.copyOf(STRINGS, STRINGS.length);
		Arrays.sort(sortedStrings);
		checkListElements(sortedStrings, sortedList);

		System.out.println("All tests successful!");
	}

	private static void checkListElements(String[] expected, StringArrayList actualList)
	{
		assertEquals("list length", expected.length, actualList.length());
		for(int i = 0; i < expected.length; i ++)
			assertEquals("index " + i, expected[i], actualList.get(i));
	}

	// A real exercise would probably use a proper testing framework like JUnit.
	// Unfortunately, JUnit so far doesn't integrate well.
	private static <T> void assertEquals(T expected, T actual)
	{
		assertEquals(null, expected, actual);
	}

	// A real exercise would probably use a proper testing framework like JUnit.
	// Unfortunately, JUnit so far doesn't integrate well.
	private static <T> void assertEquals(String context, T expected, T actual)
	{
		if(!Objects.equals(expected, actual))
			throw new RuntimeException((context != null ? context + ": " : "") + "Expected: " + expected + ", actual " + actual);
	}
}
