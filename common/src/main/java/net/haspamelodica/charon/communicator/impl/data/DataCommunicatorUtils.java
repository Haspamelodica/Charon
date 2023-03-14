package net.haspamelodica.charon.communicator.impl.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;

public class DataCommunicatorUtils
{
	public static void writeArgs(DataOutput out, List<String> params, List<LongRef> argRefs,
			Function<String, RuntimeException> createArgumentCountException, IOBiConsumer<DataOutput, LongRef> writeRef) throws IOException
	{
		int paramCount = params.size();
		if(paramCount != argRefs.size())
			throw createArgumentCountException.apply("Parameter and argument count mismatched: " + paramCount + ", " + argRefs.size());

		out.writeInt(paramCount);
		for(String param : params)
			out.writeUTF(param);
		for(LongRef argRef : argRefs)
			writeRef.accept(out, argRef);
	}

	public static Args readArgs(DataInput in, IOFunction<DataInput, LongRef> readRef) throws IOException
	{
		int paramCount = in.readInt();

		List<String> params = new ArrayList<>(paramCount);
		for(int i = 0; i < paramCount; i ++)
			params.add(in.readUTF());

		List<LongRef> argRefs = new ArrayList<>(paramCount);
		for(int i = 0; i < paramCount; i ++)
			argRefs.add(readRef.apply(in));

		return new Args(params, argRefs);
	}

	private DataCommunicatorUtils()
	{}

	public static record Args(List<String> params, List<LongRef> argRefs)
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
