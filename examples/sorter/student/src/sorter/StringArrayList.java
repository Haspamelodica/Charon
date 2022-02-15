package sorter;

import java.util.Arrays;

public class StringArrayList
{
	private final String[]	array;
	private int				length;

	public StringArrayList(int capacity)
	{
		this.array = new String[10];
	}
	public StringArrayList(String[] array, int length)
	{
		this.array = array;
		this.length = length;
	}

	public void add(String string)
	{
		if(length >= array.length)
			throw new IndexOutOfBoundsException("Capacity not high enough");
		array[length ++] = string;
	}

	public String get(int i)
	{
		if(i >= length)
			throw new IndexOutOfBoundsException();
		return array[i];
	}

	public String[] toArray()
	{
		return Arrays.copyOf(array, length);
	}
}
