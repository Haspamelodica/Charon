package net.haspamelodica.charon.communicator.impl.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;

public class DataCommunicatorUtils
{
	public static void writeArgs(DataOutput out, List<LongRef> params, List<LongRef> argRefs,
			Function<String, RuntimeException> createArgumentCountException, IOBiConsumer<DataOutput, LongRef> writeRef) throws IOException
	{
		int paramCount = params.size();
		if(paramCount != argRefs.size())
			throw createArgumentCountException.apply("Parameter and argument count mismatched: " + paramCount + ", " + argRefs.size());

		out.writeInt(paramCount);
		// index-based iteration to defend against botched List implementations
		for(int i = 0; i < paramCount; i ++)
			writeRef.accept(out, params.get(i));
		for(int i = 0; i < paramCount; i ++)
			writeRef.accept(out, argRefs.get(i));
	}

	public static Args readArgs(DataInput in, IOFunction<DataInput, LongRef> readRef) throws IOException
	{
		int paramCount = in.readInt();

		LongRef[] params = new LongRef[paramCount];
		for(int i = 0; i < paramCount; i ++)
			params[i] = readRef.apply(in);

		List<LongRef> argRefs = new ArrayList<>();
		for(int i = 0; i < paramCount; i ++)
			argRefs.add(readRef.apply(in));

		// Can't use List.of for args since some args might be null
		return new Args(List.of(params), Collections.unmodifiableList(new ArrayList<>(argRefs)));
	}

	private DataCommunicatorUtils()
	{}

	public static record Args(List<LongRef> params, List<LongRef> argRefs)
	{}

	@FunctionalInterface
	public static interface IOFunction<P, R>
	{
		public R apply(P p) throws IOException;
	}
	@FunctionalInterface
	public static interface IOBiConsumer<A, B>
	{
		public void accept(A a, B b) throws IOException;
	}
}
