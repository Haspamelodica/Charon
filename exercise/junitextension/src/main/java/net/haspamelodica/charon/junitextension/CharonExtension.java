package net.haspamelodica.charon.junitextension;

import static net.haspamelodica.charon.communicator.ClientSideCommunicatorUtils.maybeWrapLoggingIntClient;
import static net.haspamelodica.charon.communicator.ClientSideCommunicatorUtils.wrapReftransIntClient;
import static net.haspamelodica.charon.communicator.ClientSideSameJVMCommunicatorUtils.createDirectCommClient;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.withReftransParamsFunctional;
import static net.haspamelodica.charon.communicator.CommunicatorUtils.wrapTypeCaching;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import net.haspamelodica.charon.CloseableDataCommStudentSide;
import net.haspamelodica.charon.CloseableStudentSide;
import net.haspamelodica.charon.StudentSide;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.logging.CommunicationLoggerParams;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.CharonException;
import net.haspamelodica.charon.impl.StudentSideImpl;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

/**
 * A JUnit 5 {@link ParameterResolver} and {@link InvocationInterceptor} extensionimport static CommunicatorUtils
 * 
 * making an instance of {@link StudentSide} accessible to test code.
 * The extension connects with the student side according to the JUnit5 configuration parameter {@value #COMMUNICATIONARGS_PARAM_NAME}.
 */
public class CharonExtension extends TypeBasedParameterResolver<StudentSide>
{
	//TODO let user choose these
	private static final boolean	TYPECACHING	= false;
	private static final boolean	LOGGING		= false;
	private static final boolean	REFTRANS	= false;

	public static final String	CONFIGURATION_PARAMETER_NAME_BASE	= "net.haspamelodica.charon.";
	public static final String	COMMUNICATIONARGS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "communicationargs";
	public static final String	RUN_SAME_JVM_PARAM_NAME				= CONFIGURATION_PARAMETER_NAME_BASE + "unsafe.samejvm";
	public static final String	STUDENTCLASSPATH_PARAM_NAME			= CONFIGURATION_PARAMETER_NAME_BASE + "unsafe.studentclasspath";
	public static final String	USE_EXTENSION_STORE_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "useextensionstore";

	private static final Set<String>	TRUTHY_STRINGS	= Set.of("true", "1", "yes");
	private static final Set<String>	FALSY_STRINGS	= Set.of("false", "0", "no");

	private static boolean		triedResolving	= false;
	private static StudentSide	studentSide		= null;

	@Override
	public StudentSide resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
	{
		return getStudentSide(extensionContext);
	}

	private StudentSide getStudentSide(ExtensionContext extensionContext)
	{
		// It seems JUnit ExtensionContexts and their Stores only live for execution of one class.
		// So, by default, we use a static variable to store the StudentSide instance instead.
		if(extensionContext.getConfigurationParameter(USE_EXTENSION_STORE_PARAM_NAME, Boolean::parseBoolean).orElse(false))
			return getStudentSideExtensionContextStore(extensionContext);
		else
			return getStudentSideStatic(extensionContext);
	}

	private StudentSide getStudentSideExtensionContextStore(ExtensionContext extensionContext)
	{
		Store store = extensionContext.getStore(Namespace.GLOBAL);

		StudentSideCloseableResource studentSideCloseableResource = store.getOrComputeIfAbsent(
				StudentSideCloseableResource.class,
				k -> new StudentSideCloseableResource(() -> connectStudentSide(extensionContext)),
				StudentSideCloseableResource.class);

		return studentSideCloseableResource.getStudentSide();
	}

	private StudentSide getStudentSideStatic(ExtensionContext extensionContext)
	{
		if(studentSide != null)
			return studentSide;
		if(triedResolving)
			throw secondTryAfterFailException();
		triedResolving = true;
		CloseableStudentSide closeableStudentSide = connectStudentSide(extensionContext);
		studentSide = closeableStudentSide.getStudentSide();

		// We only close the communication with the student side in a JVM shutdown hook.
		// This means the student side isn't guaranteed to be closed.
		// This does not seem to be a big problem: On the exercise JVM, no cleanups are necessary
		// as the StudentSide should live on for the entire lifetime of the exercise JVM anyway.
		// Cleanups on the student side mustn't matter because the student might maliciously ignore cleanup requests.
		//TODO closing the StudentSide at some other point than a JVM shutdown hook would be "prettier".
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			try
			{
				closeableStudentSide.close();
			} catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}));

		return studentSide;
	}

	private static RuntimeException secondTryAfterFailException()
	{
		return new CharonException("Failed while creating student side before, not trying again");
	}

	private CloseableStudentSide connectStudentSide(ExtensionContext extensionContext)
	{
		Optional<String> communicationArgs = extensionContext.getConfigurationParameter(COMMUNICATIONARGS_PARAM_NAME);
		Optional<String> runsSameJvmArgs = extensionContext.getConfigurationParameter(RUN_SAME_JVM_PARAM_NAME);
		Optional<String> studentclasspathArgs = extensionContext.getConfigurationParameter(STUDENTCLASSPATH_PARAM_NAME);

		if(runsSameJvmArgs.isPresent() && parseTruthiness(runsSameJvmArgs.get()))
		{
			if(communicationArgs.isPresent())
				throw new ParameterResolutionException("\"" + RUN_SAME_JVM_PARAM_NAME
						+ "\" enabled, but \"" + COMMUNICATIONARGS_PARAM_NAME + "\" given");

			ClassLoader studentClassesClassloader = studentclasspathArgs
					.map(studentclasspath -> createStudentClassesClassloader(studentclasspath))
					.orElse(null);

			AtomicLong nextId = new AtomicLong();
			CommunicationLoggerParams loggingParams = REFTRANS
					//TODO make ALL_TO_STRING optional
					? CommunicationLoggerParams.DEFAULT_REF_TO_STRING
					: CommunicationLoggerParams.DEFAULT;
			return CloseableStudentSide.wrapIgnoringClose(new StudentSideImpl<>(
					maybeWrapTypeCaching(TYPECACHING,
							maybeWrapLoggingIntClient(LOGGING, loggingParams,
									maybeWrapReftrans(REFTRANS, false, u -> nextId.incrementAndGet(), u -> nextId.incrementAndGet(),
											createDirectCommClient(studentClassesClassloader))))));
		}

		if(communicationArgs.isEmpty())
			throw new ParameterResolutionException("Configuration parameter \""
					+ COMMUNICATIONARGS_PARAM_NAME + "\" not found and \"" + RUN_SAME_JVM_PARAM_NAME + "\" not enabled");

		if(studentclasspathArgs.isPresent())
			throw new ParameterResolutionException(RUN_SAME_JVM_PARAM_NAME + "\" not enabled, but \""
					+ STUDENTCLASSPATH_PARAM_NAME + "\" given");

		try
		{
			return new CloseableDataCommStudentSide(CommunicationArgsParser.parseSpaceSeparated(communicationArgs.get()));
		} catch(IOException e)
		{
			throw new UncheckedIOException(e);
		} catch(InterruptedException e)
		{
			throw new RuntimeException("Interrupted while connecting to student side", e);
		} catch(IncorrectUsageException e)
		{
			throw new IllegalArgumentException("Illegal value for " + COMMUNICATIONARGS_PARAM_NAME + ":"
					+ " Usage: " + CommunicationArgsParser.argsSyntax(), e);
		}
	}

	private UninitializedStudentSideCommunicator<Object, ?, ?, ?, ?, ?,
			ClientSideTransceiver<Object>, InternalCallbackManager<Object>> maybeWrapTypeCaching(boolean typecaching,
					UninitializedStudentSideCommunicator<Object, ?, ?, ?, ?, ?,
							ClientSideTransceiver<Object>, InternalCallbackManager<Object>> comm)
	{
		if(!typecaching)
			return comm;

		return wrapTypeCaching(comm);
	}

	private UninitializedStudentSideCommunicator<Object, ?, ?, ?, ?, ?,
			ClientSideTransceiver<Object>, InternalCallbackManager<Object>> maybeWrapReftrans(
					boolean reftrans,
					boolean storeRefsIdentityBased, Function<UntranslatedRef<?, ?>, Object> createForwardRef,
					Function<UntranslatedRef<?, ?>, Object> createBackwardRef,
					UninitializedStudentSideCommunicator<Object,
							?, ?, ?, ?, ?, ClientSideTransceiver<Object>, InternalCallbackManager<Object>> comm)
	{
		if(!reftrans)
			return comm;
		return withReftransParamsFunctional(storeRefsIdentityBased, createForwardRef, createBackwardRef,
				wrapReftransIntClient(
						comm));
	}

	private ClassLoader createStudentClassesClassloader(String studentclasspath)
	{
		ClassLoader justStudentClassesLoader;
		try
		{
			justStudentClassesLoader = new URLClassLoader(new URL[] {Path.of(studentclasspath).toUri().toURL()}, null);
		} catch(MalformedURLException e)
		{
			throw new ParameterResolutionException("Error creating student classes classloader", e);
		}

		return new PrioritizedClassloader(justStudentClassesLoader, CharonExtension.class.getClassLoader());
	}

	private boolean parseTruthiness(String string)
	{
		if(isTruthy(string))
			return true;
		if(isFalsy(string))
			return false;
		throw new ParameterResolutionException("Unparseable truthiness, use "
				+ TRUTHY_STRINGS.stream().collect(Collectors.joining(", ")) + " / "
				+ FALSY_STRINGS.stream().collect(Collectors.joining(", ")) + ": " + string);
	}
	private boolean isTruthy(String string)
	{
		return TRUTHY_STRINGS.contains(string.toLowerCase());
	}
	private boolean isFalsy(String string)
	{
		return FALSY_STRINGS.contains(string.toLowerCase());
	}

	private static class StudentSideCloseableResource implements CloseableResource
	{
		private final RuntimeException		error;
		private boolean						reportedErrorBefore;
		private final CloseableStudentSide	closeableStudentSide;

		public StudentSideCloseableResource(Supplier<CloseableStudentSide> closeableStudentSideSupplier)
		{
			CloseableStudentSide closeableStudentSideLocal;
			try
			{
				closeableStudentSideLocal = closeableStudentSideSupplier.get();
			} catch(RuntimeException e)
			{
				this.error = e;
				this.closeableStudentSide = null;
				return;
			}

			this.error = null;
			this.closeableStudentSide = closeableStudentSideLocal;
		}

		public StudentSide getStudentSide()
		{
			if(closeableStudentSide != null)
				return closeableStudentSide.getStudentSide();

			if(!reportedErrorBefore)
				throw error;

			throw secondTryAfterFailException();
		}

		@Override
		public void close() throws Throwable
		{
			if(closeableStudentSide != null)
				closeableStudentSide.close();
		}
	}
}
