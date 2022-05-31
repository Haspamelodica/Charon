package sorter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class StringArrayListTest
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	@Test
	public void testGet()
	{
		StringArrayList list = new StringArrayList(STRINGS.length);

		list.add("hello");
		assertEquals(list.get(0), "hello");

		list.add("string 2");
		assertEquals(list.get(0), "hello");
		assertEquals(list.get(1), "string 2");

		list.add("hello");
		assertEquals(list.get(0), "hello");
		assertEquals(list.get(1), "string 2");
		assertEquals(list.get(2), "hello");

		list.add("hello");
		assertEquals(list.get(0), "hello");
		assertEquals(list.get(1), "string 2");
		assertEquals(list.get(2), "hello");
		assertEquals(list.get(3), "hello");

		//TODO test getting illegal indices
	}
}
