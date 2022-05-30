package net.haspamelodica.charon.mockclasses.classloaders;

import static net.haspamelodica.charon.mockclasses.classloaders.ClassLoaderUtils.readAllBytes;
import static net.haspamelodica.charon.mockclasses.classloaders.ClassLoaderUtils.resourceNameForClassname;

import java.net.URL;

public class RedefiningClassLoader extends ClassLoader
{
	private final ClassLoader classfileClassLoader;

	public RedefiningClassLoader(ClassLoader parent, ClassLoader classfileClassLoader)
	{
		super(parent);
		this.classfileClassLoader = classfileClassLoader;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		URL classfileURL = classfileClassLoader.getResource(resourceNameForClassname(name));
		if(classfileURL == null)
			throw new ClassNotFoundException(name);

		byte[] classfile = readAllBytes(classfileURL);
		return defineClass(name, classfile, 0, classfile.length);
	}
}
