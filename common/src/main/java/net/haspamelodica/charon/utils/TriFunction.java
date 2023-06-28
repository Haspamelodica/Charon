package net.haspamelodica.charon.utils;

@FunctionalInterface
public interface TriFunction<A, B, C, R>
{
	public R apply(A a, B b, C c);
}
