package net.haspamelodica.charon.reflection;

import java.lang.reflect.InvocationTargetException;

public interface UnhandledReflectiveSupplier<R>
{
	public R get() throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
