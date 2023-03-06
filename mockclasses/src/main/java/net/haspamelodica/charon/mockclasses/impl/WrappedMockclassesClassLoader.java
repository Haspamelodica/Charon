package net.haspamelodica.charon.mockclasses.impl;

import static net.haspamelodica.charon.mockclasses.MockclassesUtils.pseudoAddAll;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.haspamelodica.charon.WrappedCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.PrimitiveSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;
import net.haspamelodica.charon.mockclasses.classloaders.ClassSetClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.ConstructorMethodHandler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.InstanceMethodHandler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader.StaticMethodHandler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInvocationHandler;
import net.haspamelodica.charon.mockclasses.classloaders.RedefiningClassLoader;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class WrappedMockclassesClassLoader implements AutoCloseable
{
	private final WrappedCommunicator	communicator;
	private final ClassLoader			classloader;

	public WrappedMockclassesClassLoader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider, String[] communicatorArgs,
			Class<?>... forceDelegationClasses)
			throws IOException, InterruptedException, IncorrectUsageException
	{
		this(parent, interfaceProvider, new WrappedCommunicator(communicatorArgs), forceDelegationClasses);
	}
	public WrappedMockclassesClassLoader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider,
			WrappedCommunicator communicator, Class<?>... forceDelegationClasses)
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

	public static <REF> ClassLoader createMockclassesClassloader(ClassLoader parent, DynamicInterfaceProvider interfaceProvider,
			StudentSideCommunicatorClientSide<REF> communicator, Class<?>... forceDelegationClasses)
	{
		MockclassesMarshalingTransformer<REF> transformer = new MockclassesMarshalingTransformer<>();
		//TODO feels very hardcoded. Would be fixed if we didn't prevent delegating to the parent altogether.
		Class<?>[] forceDelegationClassesWithClassesNeededByCharon = pseudoAddAll(forceDelegationClasses,
				// Delegate classes referenced by / stored in dynamically-generated classes to parent; don't define them ourself.
				// Otherwise, we get weird ClassCastExceptions.
				StaticMethodHandler.class, ConstructorMethodHandler.class, InstanceMethodHandler.class);

		//TODO rename forceDelegationClasses everywhere
		// Pass null as the parent to prevent delegating.
		//TODO instead of preventing delegating to parent, just prevent all user classes (called code) appearing in any classloader above the DynamicClassLoader,
		// and load those through an even "lower" classloader.
		ClassLoader constantClassSetClassloader = new ClassSetClassLoader(null, forceDelegationClassesWithClassesNeededByCharon);
		// InvocationHandler needs to be lazy because the initialization code of MarshalingCommunicator contains method references,
		// which use bootstrap methods, which call some standard library function, which calls getSystemClassLoader, which is not allowed
		// since we are creating the system class loader here.
		ClassLoader dynamicClassloader = new DynamicClassLoader<>(constantClassSetClassloader, interfaceProvider, transformer,
				new LazyDynamicInvocationHandler<>(() ->
				{
					// TODO make Serdeses configurable
					MarshalingCommunicator<REF> marshalingCommunicator = new MarshalingCommunicator<>(communicator, transformer,
							PrimitiveSerDes.PRIMITIVE_SERDESES).withAdditionalSerDeses(List.of(StringSerDes.class));
					return new MockclassesInvocationHandler<>(marshalingCommunicator, transformer);
				}));
		ClassLoader userClassLoader = new RedefiningClassLoader(dynamicClassloader, parent);

		transformer.setClassloader(userClassLoader);
		return userClassLoader;
	}

	private static class LazyDynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> implements DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>
	{
		private final Supplier<DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>> handlerSupplier;

		private DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> invocationHandler;

		public LazyDynamicInvocationHandler(Supplier<DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>> handlerProvider)
		{
			this.handlerSupplier = handlerProvider;
		}

		@Override
		public CCTX createClassContext(TypeDefinition type)
		{
			return handler().createClassContext(type);
		}

		@Override
		public SCTX createStaticMethodContext(CCTX classContext, MethodDescription method)
		{
			return handler().createStaticMethodContext(classContext, method);
		}

		@Override
		public TCTX createConstructorContext(CCTX classContext, MethodDescription constructor)
		{
			return handler().createConstructorContext(classContext, constructor);
		}

		@Override
		public MCTX createInstanceMethodContext(CCTX classContext, MethodDescription method)
		{
			return handler().createInstanceMethodContext(classContext, method);
		}

		@Override
		public void registerDynamicClassCreated(CCTX classContext, Class<?> clazz)
		{
			handler().registerDynamicClassCreated(classContext, clazz);
		}

		@Override
		public Object invokeStaticMethod(CCTX classContext, SCTX methodContext, Object[] args)
		{
			return handler().invokeStaticMethod(classContext, methodContext, args);
		}

		@Override
		public ICTX invokeConstructor(CCTX classContext, TCTX constructorContext, Object receiver, Object[] args)
		{
			return handler().invokeConstructor(classContext, constructorContext, receiver, args);
		}

		@Override
		public Object invokeInstanceMethod(CCTX classContext, MCTX methodContext, Object receiver, ICTX receiverContext, Object[] args)
		{
			return handler().invokeInstanceMethod(classContext, methodContext, receiver, receiverContext, args);
		}

		private DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX> handler()
		{
			if(invocationHandler != null)
				return invocationHandler;

			synchronized(this)
			{
				if(invocationHandler != null)
					return invocationHandler;
				return invocationHandler = handlerSupplier.get();
			}
		}
	}
}
