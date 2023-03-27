package net.haspamelodica.charon.reflection;

import java.lang.reflect.InvocationTargetException;

public interface UnhandledReflectiveFunction<A, R>
{
	public R apply(A a) throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
