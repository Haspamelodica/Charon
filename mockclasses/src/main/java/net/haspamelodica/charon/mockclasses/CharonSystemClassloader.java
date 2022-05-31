package net.haspamelodica.charon.mockclasses;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.ClasspathBasedDynamicInterfaceProvider;
import net.haspamelodica.charon.mockclasses.impl.WrappedMockclassesClassLoader;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;
import static net.haspamelodica.charon.mockclasses.MockclassesUtils.*;

public class CharonSystemClassloader extends URLClassLoader
{
	//TODO duplicated from CharonExtension
	public static final String	CONFIGURATION_PARAMETER_NAME_BASE	= "net.haspamelodica.charon.";
	public static final String	COMMUNICATIONARGS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "communicationargs";
	public static final String	TEMPLATE_CODE_URLS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "templatecode.urls";
	public static final String	TEMPLATE_CODE_PATHS_PARAM_NAME		= CONFIGURATION_PARAMETER_NAME_BASE + "templatecode.paths";
	public static final String	TEMPLATE_CODE_CLASSES_PARAM_NAME	= CONFIGURATION_PARAMETER_NAME_BASE + "templatecode.classes";

	public CharonSystemClassloader(ClassLoader parent) throws IncorrectUsageException, IOException, InterruptedException
	{
		super(new URL[0], createAndRegisterMockclassesClassLoader(parent));
	}

	private static ClassLoader createAndRegisterMockclassesClassLoader(ClassLoader parent)
			throws IncorrectUsageException, IOException, InterruptedException
	{
		List<URL> dynamicInterfaceClasspath = new ArrayList<>();

		String templateCodeURLsString = System.getProperty(TEMPLATE_CODE_URLS_PARAM_NAME, "");
		if(!templateCodeURLsString.equals(""))
			dynamicInterfaceClasspath.addAll(Arrays.stream(templateCodeURLsString.split(" ")).map(url ->
			{
				try
				{
					return new URL(url);
				} catch(MalformedURLException e)
				{
					throw new RuntimeException("Malformed URL in property " + TEMPLATE_CODE_URLS_PARAM_NAME + ": " + url, e);
				}
			}).collect(Collectors.toList()));

		String templateCodePathsString = System.getProperty(TEMPLATE_CODE_PATHS_PARAM_NAME, "");
		if(!templateCodePathsString.equals(""))
			dynamicInterfaceClasspath.addAll(Arrays.stream(templateCodePathsString.split(" ")).map(path ->
			{
				try
				{
					return Path.of(path).toUri().toURL();
				} catch(MalformedURLException e)
				{
					throw new RuntimeException("Malformed URL when converting path to URL in property " + TEMPLATE_CODE_PATHS_PARAM_NAME + ": " + path, e);
				}
			}).collect(Collectors.toList()));

		String templateCodeClassesString = System.getProperty(TEMPLATE_CODE_CLASSES_PARAM_NAME, "");
		if(!templateCodeClassesString.equals(""))
			dynamicInterfaceClasspath.addAll(Arrays.stream(templateCodeClassesString.split(" "))
					.map(classname ->
					{
						try
						{
							return classpathUrlForClass(Class.forName(classname));
						} catch(ClassNotFoundException e1)
						{
							throw new RuntimeException("Class not found in property " + TEMPLATE_CODE_CLASSES_PARAM_NAME + ": " + classname, e1);
						}
					}).collect(Collectors.toList()));

		if(dynamicInterfaceClasspath.size() == 0)
		{
			System.err.println("Set at least one of the properties \"" + TEMPLATE_CODE_URLS_PARAM_NAME + "\", \""
					+ TEMPLATE_CODE_PATHS_PARAM_NAME + "\", and \""
					+ TEMPLATE_CODE_CLASSES_PARAM_NAME + "\" to a space-separated list of URLs, paths, and classes, respectively. "
					+ "The expected student code interface will get searched for in the given URLs and paths, "
					+ "as well as the classpaths the given classes come from.");
			throw new IncorrectUsageException("No dynamic interface classpath given; see above for usage");
		}

		String communicatorArgsString = System.getProperty(COMMUNICATIONARGS_PARAM_NAME);
		if(communicatorArgsString == null)
		{
			System.err.println("Set the property \"" + COMMUNICATIONARGS_PARAM_NAME + "\": " + CommunicationArgsParser.argsSyntax());
			throw new IncorrectUsageException("Property " + COMMUNICATIONARGS_PARAM_NAME + " not set");
		}
		String[] communicatorArgs = communicatorArgsString.split(" ");

		DynamicInterfaceProvider interfaceProvider = new ClasspathBasedDynamicInterfaceProvider(dynamicInterfaceClasspath.toArray(URL[]::new));

		WrappedMockclassesClassLoader wrappedMockclassesClassLoader;
		try
		{
			wrappedMockclassesClassLoader = new WrappedMockclassesClassLoader(parent, interfaceProvider, communicatorArgs);
		} catch(IncorrectUsageException e)
		{
			System.err.println("Incorrect syntax of \"" + COMMUNICATIONARGS_PARAM_NAME + "\": " + e.getMessage());
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
