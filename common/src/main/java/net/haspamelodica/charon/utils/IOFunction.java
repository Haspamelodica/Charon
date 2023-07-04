package net.haspamelodica.charon.utils;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<P, R>
{
	public R apply(P p) throws IOException;
}
