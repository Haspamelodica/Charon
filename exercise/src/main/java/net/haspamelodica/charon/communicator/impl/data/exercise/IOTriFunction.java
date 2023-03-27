package net.haspamelodica.charon.communicator.impl.data.exercise;

import java.io.IOException;

public interface IOTriFunction<A, B, C, R>
{
	public R apply(A a, B b, C c) throws IOException;
}
