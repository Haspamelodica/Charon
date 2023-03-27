package net.haspamelodica.charon.reflection;

public interface UnhandledReflectiveSupplierNoInvocationTargetException<R> extends UnhandledReflectiveSupplier<R>
{
	@Override
	public R get() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			NoSuchMethodException, NoSuchFieldException, SecurityException, ClassNotFoundException;
}
