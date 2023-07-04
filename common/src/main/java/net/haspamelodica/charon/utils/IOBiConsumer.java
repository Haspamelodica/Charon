package net.haspamelodica.charon.utils;

import java.io.IOException;

@FunctionalInterface
public interface IOBiConsumer<A, B>
{
	public void accept(A a, B b) throws IOException;
}
