package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDefinition.Sort;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInterfaceProvider;

public class ClasspathBasedDynamicInterfaceProvider implements DynamicInterfaceProvider
{
	private final ClassLoader dynamicInterfaceClassloader;

	public ClasspathBasedDynamicInterfaceProvider(URL... dynamicInterfaceClasspath)
	{
		// pass null as parent to declare to not use any parent
		this.dynamicInterfaceClassloader = new URLClassLoader(dynamicInterfaceClasspath, null);
	}

	@Override
	public TypeDefinition typeDefinitionFor(String classname)
	{
		Class<?> clazz;
		try
		{
			clazz = dynamicInterfaceClassloader.loadClass(classname);
		} catch(ClassNotFoundException e)
		{
			// this class does not have an expected interface; ignore exception
			return null;
		}

		return typeDescriptionFor(clazz);
	}

	public static TypeDefinition typeDescriptionFor(Type type)
	{
		return Sort.describe(type);
	}
}
