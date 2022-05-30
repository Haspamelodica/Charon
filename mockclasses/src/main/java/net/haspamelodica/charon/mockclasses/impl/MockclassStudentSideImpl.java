package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;

public class MockclassStudentSideImpl implements MockclassStudentSide
{
	private final ClassLoader classloader;

	public MockclassStudentSideImpl(ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	@Override
	public <INTERFACE, IMPL extends INTERFACE, X extends Exception> INTERFACE
			createInstanceWithMockclasses(Class<INTERFACE> interfaceClass, Class<IMPL> implementationClass,
					List<Class<?>> parameterClasses, Object... arguments) throws X
	{
		// This is not Class<IMPL>, but Class<other class with same name and interface as IMPL>.
		// At compile-time, those are indistinguishable, and at runtime, types are erased, so it wouldn't matter,
		// but this way is cleaner.
		Class<? extends INTERFACE> implClassWithMockclasses;
		try
		{
			// assume the classfile found "inside" the classloader matches the class outside.
			// Also, INTERFACE is forced to be delegated, as per method contract.
			@SuppressWarnings("unchecked")
			Class<? extends INTERFACE> implClassWithMockclassesCasted =
					(Class<? extends INTERFACE>) classloader.loadClass(implementationClass.getName());
			implClassWithMockclasses = implClassWithMockclassesCasted;
		} catch(ClassNotFoundException e)
		{
			//TODO better exception type
			throw new RuntimeException("Implementation class not found within mockclasses classloader", e);
		}

		Constructor<? extends INTERFACE> constructor;
		try
		{
			constructor = implClassWithMockclasses.getConstructor();
		} catch(NoSuchMethodException e)
		{
			//TODO better exception type
			throw new RuntimeException("Constructor with given parameters doesn't exist or isn't visible", e);
		}

		INTERFACE instance;
		try
		{
			instance = constructor.newInstance();
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			//TODO better exception type
			throw new RuntimeException("Instance creation failed", e);
		}

		// catch user errors
		if(!interfaceClass.isInstance(instance))
			//TODO better exception type
			throw new RuntimeException("Created instance is not an instance of the given interface; "
					+ "is the interface forced to be delegated?");

		return instance;
	}
}
