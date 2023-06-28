package net.haspamelodica.charon.junitextension;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class PrioritizedClassloader extends ClassLoader
{
	private final List<ClassLoader> classloaders;

	public PrioritizedClassloader(ClassLoader... classloaders)
	{
		super(null);
		this.classloaders = List.of(classloaders);
	}
	public PrioritizedClassloader(List<ClassLoader> classloaders)
	{
		super(null);
		this.classloaders = List.copyOf(classloaders);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		List<ClassNotFoundException> classNotFoundExceptions = new ArrayList<>();
		for(ClassLoader classloader : classloaders)
			try
			{
				return classloader.loadClass(name);
			} catch(ClassNotFoundException e)
			{
				classNotFoundExceptions.add(e);
			}

		ClassNotFoundException classNotFoundException = new ClassNotFoundException(name);
		classNotFoundExceptions.forEach(classNotFoundException::addSuppressed);
		throw classNotFoundException;
	}

	@Override
	protected URL findResource(String name)
	{
		for(ClassLoader classloader : classloaders)
		{
			URL resource = classloader.getResource(name);
			if(resource != null)
				return resource;
		}
		return null;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException
	{
		Iterator<URL> iterator = classloaders
				.stream()
				.flatMap(classloader -> classloader.resources(name))
				.iterator();

		return new Enumeration<URL>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public URL nextElement()
			{
				return iterator.next();
			}
		};
	}
}
