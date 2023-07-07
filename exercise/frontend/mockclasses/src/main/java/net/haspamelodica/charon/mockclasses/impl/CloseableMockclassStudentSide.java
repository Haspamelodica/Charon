package net.haspamelodica.charon.mockclasses.impl;

import java.io.IOException;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;


public class CloseableMockclassStudentSide implements AutoCloseable
{
	private final CloseableMockclassesClassLoader	closeableClassLoader;
	private final MockclassStudentSide				mockclassStudentSide;

	public CloseableMockclassStudentSide(ClassLoader parent, DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(new CloseableMockclassesClassLoader(parent, interfaceProvider, communicatorArgs, forceDelegationClasses));
	}
	public CloseableMockclassStudentSide(CloseableMockclassesClassLoader closeableClassLoader)
	{
		this.closeableClassLoader = closeableClassLoader;
		this.mockclassStudentSide = new MockclassStudentSideImpl(closeableClassLoader.getClassloader());
	}

	public MockclassStudentSide getStudentSide()
	{
		return mockclassStudentSide;
	}

	@Override
	public void close() throws IOException
	{
		closeableClassLoader.close();
	}
}
