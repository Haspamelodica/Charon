package sorter;

import java.util.Arrays;

public class Sorter
{
	private static final boolean BUG = false;

	public static StringArrayList sort(StringArrayList list)
	{
		String[] array = list.toArray();
		Arrays.sort(array);

		if(BUG)
		{
			String tmp = array[1];
			array[1] = array[2];
			array[2] = tmp;
		}

		return new StringArrayList(array, array.length);
	}

}
