package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDefinition.Sort;

public class ClasspathBasedDynamicInterfaceProvider implements DynamicInterfaceProvider
{
	private final ClassLoader dynamicInterfaceClassloader;

	public ClasspathBasedDynamicInterfaceProvider(URL... dynamicInterfaceClasspath)
	{
		// no parent
		this.dynamicInterfaceClassloader = new URLClassLoader(dynamicInterfaceClasspath, null);
	}

	@Override
	public ClassInterface interfaceForClass(String classname)
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

		return new ClassInterface(classname,
				Arrays.stream(clazz.getDeclaredMethods()).map(this::interfaceForMethod).toList(),
				Arrays.stream(clazz.getDeclaredConstructors()).map(this::interfaceForConstructor).toList());
	}

	private MethodInterface interfaceForMethod(Method method)
	{
		return new MethodInterface(method.getName(),
				typeDescr(method.getGenericReturnType()),
				Modifier.isStatic(method.getModifiers()),
				Arrays.stream(method.getGenericParameterTypes()).map(t -> typeDescr(t)).toList());
	}
	private ConstructorInterface interfaceForConstructor(Constructor<?> constructor)
	{
		return new ConstructorInterface(Arrays.stream(constructor.getGenericParameterTypes()).map(t -> typeDescr(t)).toList());
	}

	private static TypeDefinition typeDescr(Type type)
	{
		return Sort.describe(type);
	}
}
