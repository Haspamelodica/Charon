package net.haspamelodica.charon.reflection;

public interface UnhandledReflectiveRunnableNoInvocationTargetException extends UnhandledReflectiveRunnable
{
	@Override
	public void run() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
