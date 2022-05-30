package net.haspamelodica.charon.mockclasses.classloaders;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassSetClassLoader extends ClassLoader
{
	private final Map<String, Class<?>> classSet;

	public ClassSetClassLoader(ClassLoader parent, Class<?>... classSet)
	{
		this(parent, Arrays.stream(classSet));
	}
	public ClassSetClassLoader(ClassLoader parent, Set<Class<?>> classSet)
	{
		this(parent, classSet.stream());
	}
	public ClassSetClassLoader(ClassLoader parent, Stream<Class<?>> classSet)
	{
		super(parent);
		this.classSet = classSet.collect(Collectors.toUnmodifiableMap(Class::getName, c -> c));
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> clazz = classSet.get(name);
		if(clazz != null)
			return clazz;
		throw new ClassNotFoundException(name);
	}
}
