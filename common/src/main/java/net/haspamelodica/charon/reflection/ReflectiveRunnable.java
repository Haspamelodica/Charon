package net.haspamelodica.charon.reflection;

import java.lang.reflect.InvocationTargetException;

public interface ReflectiveRunnable
{
	public void run() throws InvocationTargetException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
