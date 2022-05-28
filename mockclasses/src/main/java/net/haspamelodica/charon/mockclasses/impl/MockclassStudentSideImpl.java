package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;

public class MockclassStudentSideImpl implements MockclassStudentSide
{
	private final ClassLoader classloader;

	public MockclassStudentSideImpl(ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	/**
	 * Runs the specified action with the given parameters with mockclasses enabled.
	 * The specified action class has to have a public constructor with no parameters.
	 * The parameter and return classes have to be forced to be delegated.
	 */
	@Override
	public <P, R, X extends Exception> R runWithMockclasses(Class<? extends MockclassesFunction<P, R, X>> actionClass,
			P params) throws X
	{
		Class<MockclassesFunction<P, R, X>> actionClassWithMockclasses;
		try
		{
			// assume the classfile found "inside" the classloader matches the class outside.
			// Also, MockclassesRunnable is forced to be delegated.
			@SuppressWarnings("unchecked")
			Class<MockclassesFunction<P, R, X>> actionClassWithMockclassesCasted =
					(Class<MockclassesFunction<P, R, X>>) classloader.loadClass(actionClass.getName());
			actionClassWithMockclasses = actionClassWithMockclassesCasted;
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException("Action class not found withing mockclasses classloader", e);
		}

		Constructor<MockclassesFunction<P, R, X>> constructor;
		try
		{
			constructor = actionClassWithMockclasses.getConstructor();
		} catch(NoSuchMethodException e)
		{
			throw new RuntimeException("Constructor with no parameters doesn't exist or isn't visible", e);
		}

		MockclassesFunction<P, R, X> actionInstance;
		try
		{
			actionInstance = constructor.newInstance();
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Instance creation failed", e);
		}

		return actionInstance.apply(params);
	}
}
