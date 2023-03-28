package net.haspamelodica.charon.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOFunction<A, R>
{
	public R apply(A a) throws IOException;
}
