package net.haspamelodica.charon.mockclasses.impl;

import static net.haspamelodica.charon.mockclasses.impl.ArrayUtils.pseudoAdd;

import java.io.IOException;

import net.haspamelodica.charon.mockclasses.MockclassStudentSide;
import net.haspamelodica.charon.mockclasses.MockclassStudentSide.MockclassesFunction;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInterfaceProvider;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;


public class WrappedMockclassStudentSide implements AutoCloseable
{
	private final WrappedMockclassesClassLoader	wrappedClassLoader;
	private final MockclassStudentSide			mockclassStudentSide;

	public WrappedMockclassStudentSide(DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses) throws IOException, InterruptedException, IncorrectUsageException
	{
		this(new WrappedMockclassesClassLoader(interfaceProvider, communicatorArgs,
				// force delegation of MockclassesFunction, because MockclassStudentSideImpl needs that
				pseudoAdd(forceDelegationClasses, MockclassesFunction.class)));
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
