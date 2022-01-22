package net.haspamelodica.studentcodeseparator.reflection;

import java.lang.reflect.InvocationTargetException;

public interface ThrowingSupplier<R>
{
	public R get() throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}