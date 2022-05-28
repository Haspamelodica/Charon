package net.haspamelodica.charon.mockclasses.impl;

import static net.haspamelodica.charon.mockclasses.impl.ArrayUtils.pseudoAdd;

import java.io.IOException;

import net.haspamelodica.charon.WrappedCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.marshaling.Marshaler;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInvocationHandler;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedMockclassesClassLoader implements AutoCloseable
{
	private final WrappedCommunicator<?>	communicator;
	private final ClassLoader				classloader;

	public WrappedMockclassesClassLoader(DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses)
			throws IOException, InterruptedException, IncorrectUsageException
	{
		this(interfaceProvider, new WrappedCommunicator<>(communicatorArgs), forceDelegationClasses);
	}
	public WrappedMockclassesClassLoader(DynamicInterfaceProvider interfaceProvider,
			WrappedCommunicator<Ref<Integer, Object>> communicator, Class<?>... forceDelegationClasses)
	{
		this.communicator = communicator;
		this.classloader = createMockclassesClassloader(interfaceProvider, communicator.getClient(), forceDelegationClasses);
	}

	public ClassLoader getClassloader()
	{
		return classloader;
	}

	@Override
	public void close() throws IOException
	{
		communicator.close();
	}

	public static ClassLoader createMockclassesClassloader(DynamicInterfaceProvider interfaceProvider,
			StudentSideCommunicatorClientSide<Ref<Integer, Object>> communicator, Class<?>... forceDelegationClasses)
	{
		MockclassesMarshalingTransformer<Ref<Integer, Object>> transformer = new MockclassesMarshalingTransformer<>(communicator);
		Marshaler<?, ?, Ref<Integer, Object>> marshaler = new Marshaler<>(communicator, transformer,
				PrimitiveSerDes.PRIMITIVE_SERDESES);
		DynamicInvocationHandler<?, ?, ?, ?, ?> invocationHandler = new MockclassesInvocationHandler<>(communicator, marshaler, transformer);

		// Mockclass has to be delegated because classes from the "outer" classloader
		// need to cast mock classes to "their" Mockclass class.
		Class<?>[] forceDelegationClassesWithMockclass = pseudoAdd(forceDelegationClasses, Mockclass.class);

		ClassLoader classloader = new DynamicClassLoader<>(interfaceProvider, transformer, false,
				invocationHandler, forceDelegationClassesWithMockclass);
		transformer.setClassloader(classloader);

		return classloader;
	}
}
