package sorter.junittests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.junitextension.CharonExtension;
import sorter.StringArrayList;

@ExtendWith(CharonExtension.class)
public class StringArrayListTest
{
	private static final String[] STRINGS = {"Hello", "ABC", "DEF", "DE", "XYZ", "Xyz", "xyz", "xYZ"};

	private static StringArrayList.Prototype StringArrayListP;

	@BeforeAll
	public static void initStringArrayListP(StudentSide studentSide)
	{
		StringArrayListP = studentSide.createPrototype(StringArrayList.Prototype.class);
	}

	@Test
	public void testAdd()
	{
		StringArrayList list = StringArrayListP.new_(STRINGS.length);
		assertEquals(0, list.length());

		System.gc();

		list.add("hello");
		assertEquals(1, list.length());

		list.add("string 2");
		assertEquals(2, list.length());

		list.add("hello");
		assertEquals(3, list.length());

		list.add("hello");
		assertEquals(4, list.length());
	}

	@Test
	public void testGet()
	{
		StringArrayList list = StringArrayListP.new_(STRINGS.length);

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
