package net.haspamelodica.charon.reflection;

public interface UnhandledReflectiveFunctionNoInvocationTargetException<A, R> extends UnhandledReflectiveFunction<A, R>
{
	@Override
	public R apply(A a) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
