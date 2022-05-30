package net.haspamelodica.charon.mockclasses;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.ClasspathBasedDynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.WrappedMockclassesClassLoader;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class CharonSystemClassloader extends URLClassLoader
{
	//TODO duplicated from CharonExtension
	public static final String	CONFIGURATION_PARAMETER_NAME_BASE	= "net.haspamelodica.charon.";
	public static final String	COMMUNICATIONARGS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "communicationargs";
	public static final String	TEMPLATE_CODE_URLS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "templatecodeurls";

	public CharonSystemClassloader(ClassLoader parent) throws IncorrectUsageException, IOException, InterruptedException
	{
		super(new URL[0], createAndRegisterMockclassesClassLoader(parent));
	}

	private static ClassLoader createAndRegisterMockclassesClassLoader(ClassLoader parent)
			throws IncorrectUsageException, IOException, InterruptedException
	{
		String templateCodeURLsString = System.getProperty(TEMPLATE_CODE_URLS_PARAM_NAME);
		if(templateCodeURLsString == null)
		{
			System.err.println("Set the property \"" + TEMPLATE_CODE_URLS_PARAM_NAME + "\" to a space-separated list of URLs "
					+ "containing classes of which the expected student code interface should be read from");
			throw new IncorrectUsageException();
		}
		URL[] dynamicInterfaceClasspath = Arrays.stream(templateCodeURLsString.split(" ")).map(url ->
		{
			try
			{
				return new URL(url);
			} catch(MalformedURLException e)
			{
				throw new RuntimeException("Malformed URL in property " + TEMPLATE_CODE_URLS_PARAM_NAME + ": " + url, e);
			}
		}).toArray(URL[]::new);

		String communicatorArgsString = System.getProperty(COMMUNICATIONARGS_PARAM_NAME);
		if(communicatorArgsString == null)
		{
			System.err.println("Set the property \"" + COMMUNICATIONARGS_PARAM_NAME + "\": " + CommunicationArgsParser.argsSyntax());
			throw new IncorrectUsageException();
		}
		String[] communicatorArgs = communicatorArgsString.split(" ");

		DynamicInterfaceProvider interfaceProvider = new ClasspathBasedDynamicInterfaceProvider(dynamicInterfaceClasspath);

		WrappedMockclassesClassLoader wrappedMockclassesClassLoader;
		try
		{
			wrappedMockclassesClassLoader = new WrappedMockclassesClassLoader(parent, interfaceProvider, communicatorArgs);
		} catch(IncorrectUsageException e)
		{
			System.err.println("Set the property \"" + COMMUNICATIONARGS_PARAM_NAME + "\": " + CommunicationArgsParser.argsSyntax());
			throw e;
		}

		//TODO don't use a system shutdown hook, but I don't think there's a better way.
		// Also, the student side could maliciously ignore the shutdown command either way.
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			try
			{
				wrappedMockclassesClassLoader.close();
			} catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}));

		return wrappedMockclassesClassLoader.getClassloader();
	}

	/**
	 * Called by the VM to support dynamic additions to the class path
	 *
	 * @see java.lang.instrument.Instrumentation#appendToSystemClassLoaderSearch
	 */
	void appendToClassPathForInstrumentation(String path)
	{
		try
		{
			addURL(Path.of(path).toUri().toURL());
		} catch(MalformedURLException e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}
