package net.haspamelodica.charon.mockclasses.impl;

import static net.haspamelodica.charon.mockclasses.impl.ArrayUtils.pseudoAddAll;

import java.io.IOException;

import net.haspamelodica.charon.WrappedCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.marshaling.Marshaler;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.mockclasses.classloaders.ClassSetClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInvocationHandler;
import net.haspamelodica.charon.mockclasses.classloaders.RedefiningClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.ConstructorMethodHandler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.InstanceMethodHandler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.StaticMethodHandler;
import net.haspamelodica.charon.refs.Ref;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedMockclassesClassLoader implements AutoCloseable
{
	private final WrappedCommunicator<?>	communicator;
	private final ClassLoader				classloader;

	public WrappedMockclassesClassLoader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses)
			throws IOException, InterruptedException, IncorrectUsageException
	{
		this(parent, interfaceProvider, new WrappedCommunicator<>(communicatorArgs), forceDelegationClasses);
	}
	public WrappedMockclassesClassLoader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider,
			WrappedCommunicator<Ref<Integer, Object>> communicator, Class<?>... forceDelegationClasses)
	{
		this.communicator = communicator;
		this.classloader = createMockclassesClassloader(parent, interfaceProvider, communicator.getClient(), forceDelegationClasses);
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

	public static ClassLoader createMockclassesClassloader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider,
			StudentSideCommunicatorClientSide<Ref<Integer, Object>> communicator, Class<?>... forceDelegationClasses)
	{
		MockclassesMarshalingTransformer<Ref<Integer, Object>> transformer = new MockclassesMarshalingTransformer<>(communicator);
		Marshaler<?, ?, Ref<Integer, Object>> marshaler = new Marshaler<>(communicator, transformer,
				PrimitiveSerDes.PRIMITIVE_SERDESES);
		DynamicInvocationHandler<?, ?, ?, ?, ?> invocationHandler = new MockclassesInvocationHandler<>(communicator, marshaler, transformer);

		//TODO feels very hardcoded. Would be fixed if we didn't prevent delegating to the parent altogether.
		Class<?>[] forceDelegationClassesWithClassesNeededByCharon = pseudoAddAll(forceDelegationClasses,
				// Delegate Ref because of the getRef method of Mockclass
				Ref.class,
				// Mockclass has to be delegated
				// because classes from the "outer" classloader need to cast mock classes to "their" Mockclass class.
				Mockclass.class,
				// Delegate classes referenced by / stored in dynamically-generated classes to parent; don't define them ourself.
				// Otherwise, we get weird ClassCastExceptions.
				StaticMethodHandler.class, ConstructorMethodHandler.class, InstanceMethodHandler.class);

		//TODO rename forceDelegationClasses everywhere
		// Pass null as the parent to prevent delegating.
		//TODO instead of preventing delegating to parent, just prevent all user classes (called code) appearing in any classloader above the DynamicClassLoader,
		// and load those through an even "lower" classloader.
		ClassLoader constantClassSetClassloader = new ClassSetClassLoader(null, forceDelegationClassesWithClassesNeededByCharon);
		ClassLoader dynamicClassloader = new DynamicClassLoader<>(constantClassSetClassloader, interfaceProvider, transformer, invocationHandler);
		ClassLoader userClassLoader = new RedefiningClassLoader(dynamicClassloader, parent);

		transformer.setClassloader(userClassLoader);
		return userClassLoader;
	}
}
