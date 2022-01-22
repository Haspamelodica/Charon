package net.haspamelodica.studentcodeseparator.reflection;

import java.lang.reflect.InvocationTargetException;

public interface ThrowingFunction<A, R>
{
	public R apply(A a) throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}