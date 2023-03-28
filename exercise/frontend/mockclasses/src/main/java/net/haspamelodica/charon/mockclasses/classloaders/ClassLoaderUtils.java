package net.haspamelodica.charon.mockclasses.classloaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClassLoaderUtils
{
	/**
	 * Copied from {@link jdk.internal.loader.BuiltinClassLoader#findClassOnClassPathOrNull}
	 */
	public static String resourceNameForClassname(String name)
	{
		return name.replace('.', '/') + ".class";
	}

	public static byte[] readAllBytes(URL resource) throws ClassNotFoundException
	{
		try(InputStream in = resource.openStream())
		{
			return in.readAllBytes();
		} catch(IOException e)
		{
			throw new ClassNotFoundException("Could not read bytes of " + resource, e);
		}
	}

	private ClassLoaderUtils()
	{}
}
