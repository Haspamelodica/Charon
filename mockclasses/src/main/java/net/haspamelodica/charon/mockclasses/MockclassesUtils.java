package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDefinition.Sort;

public class MockclassesUtils
{
	@SafeVarargs
	public static <E> E[] pseudoAddAll(E[] original, E... added)
	{
		E[] merged = Arrays.copyOf(original, original.length + added.length);
		System.arraycopy(added, 0, merged, original.length, added.length);
		return merged;
	}

	public static <E> E[] pseudoAdd(E[] original, E added)
	{
		E[] merged = Arrays.copyOf(original, original.length + 1);
		merged[original.length] = added;
		return merged;
	}

	public static URL classpathUrlForClass(Class<?> clazz)
	{
		String classFilename = clazz.getName().replace('.', '/') + ".class";
		URL classUrl = clazz.getClassLoader().getResource(classFilename);
		if(classUrl == null)
			//TODO better exception type
			throw new RuntimeException("Couldn't find original classfile resource: " + clazz);

		String path = classUrl.getPath();
		if(!path.endsWith(classFilename))
			//TODO better exception type
			throw new RuntimeException("Original classfile resource doesn't end in qualified class name: " + clazz);

		String classpathPath = path.substring(0, path.length() - classFilename.length());
		// pass path as file: strip query and ref, if any
		try
		{
			return new URL(classUrl.getProtocol(), classUrl.getHost(), classUrl.getPort(), classpathPath);
		} catch(MalformedURLException e)
		{
			//TODO better exception type
			throw new RuntimeException(e);
		}
	}

	public static TypeDefinition typeDefinitionFor(Type type)
	{
		return Sort.describe(type);
	}

	private MockclassesUtils()
	{}
}
