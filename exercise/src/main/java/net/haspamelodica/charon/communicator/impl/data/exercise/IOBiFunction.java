package net.haspamelodica.charon.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOBiFunction<A, B, R>
{
	public R apply(A a, B b) throws IOException;
}
