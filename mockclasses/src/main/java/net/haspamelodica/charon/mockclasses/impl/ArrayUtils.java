package net.haspamelodica.charon.mockclasses.impl;

import java.util.Arrays;

public class ArrayUtils
{
	@SafeVarargs
	public static <E> E[] pseudoAddAll(E[] original, E... added)
	{
		E[] merged = Arrays.copyOf(original, original.length + added.length);
		System.arraycopy(added, 0, merged, original.length, added.length);
		return merged;
	}

	public static <E> E[] pseudoAdd(E[] original, E added)
	{
		E[] merged = Arrays.copyOf(original, original.length + 1);
		merged[original.length] = added;
		return merged;
	}

	private ArrayUtils()
	{}
}
