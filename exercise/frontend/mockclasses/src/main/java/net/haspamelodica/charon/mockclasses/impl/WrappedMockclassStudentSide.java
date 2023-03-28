package net.haspamelodica.charon.mockclasses.impl;

import java.io.IOException;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;


public class WrappedMockclassStudentSide implements AutoCloseable
{
	private final WrappedMockclassesClassLoader	wrappedClassLoader;
	private final MockclassStudentSide			mockclassStudentSide;

	public WrappedMockclassStudentSide(ClassLoader parent, DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(new WrappedMockclassesClassLoader(parent, interfaceProvider, communicatorArgs, forceDelegationClasses));
	}
	public WrappedMockclassStudentSide(WrappedMockclassesClassLoader wrappedClassLoader)
	{
		this.wrappedClassLoader = wrappedClassLoader;
		this.mockclassStudentSide = new MockclassStudentSideImpl(wrappedClassLoader.getClassloader());
	}

	public MockclassStudentSide getStudentSide()
	{
		return mockclassStudentSide;
	}

	@Override
	public void close() throws IOException
	{
		wrappedClassLoader.close();
	}
}
