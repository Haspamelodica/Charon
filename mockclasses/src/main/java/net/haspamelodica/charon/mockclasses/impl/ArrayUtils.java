package net.haspamelodica.charon.mockclasses.impl;

import java.util.Arrays;

public class ArrayUtils
{
	public static <E> E[] pseudoAdd(E[] original, E added)
	{
		E[] merged = Arrays.copyOf(original, original.length + 1);
		merged[original.length] = added;
		return merged;
	}

	private ArrayUtils()
	{}
}
