package net.haspamelodica.charon.communicator.impl.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.utils.IOBiConsumer;
import net.haspamelodica.charon.utils.IOFunction;

public class DataCommunicatorUtils
{
	public static <E> int writeList(DataOutput out, List<E> list, IOBiConsumer<DataOutput, E> writeE) throws IOException
	{
		int count = list.size();
		out.writeInt(count);
		writeNUnchecked(out, list, count, writeE);
		return count;
	}

	public static <E> void writeN(DataOutput out, List<E> list, int n,
			Function<String, RuntimeException> createArgumentCountException, IOBiConsumer<DataOutput, E> writeE) throws IOException
	{
		int count = list.size();
		if(count != n)
			throw createArgumentCountException.apply("Unexpected list size: expected " + n + ", but was " + count);

		writeNUnchecked(out, list, n, writeE);
	}

	public static <E> void writeNUnchecked(DataOutput out, List<E> list, int n, IOBiConsumer<DataOutput, E> writeE) throws IOException
	{
		// index-based iteration to defend against botched List implementations
		for(int i = 0; i < n; i ++)
			writeE.accept(out, list.get(i));
	}

	public static <E> List<E> readList(DataInput in, IOFunction<DataInput, E> readE) throws IOException
	{
		return readN(in, in.readInt(), readE);
	}

	public static <E> List<E> readN(DataInput in, int n, IOFunction<DataInput, E> readE) throws IOException
	{
		List<E> list = new ArrayList<>(n);
		for(int i = 0; i < n; i ++)
			list.add(readE.apply(in));

		// Can't use List.of or List.copyOf because some elements might be null
		return Collections.unmodifiableList(list);
	}

	private DataCommunicatorUtils()
	{}
}
