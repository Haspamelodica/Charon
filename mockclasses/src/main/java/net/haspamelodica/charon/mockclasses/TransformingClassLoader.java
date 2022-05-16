package net.haspamelodica.charon.mockclasses;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class TransformingClassLoader extends ClassLoader
{
	/**
	 * This method is called for every class to be loaded under a {@link TransformingClassLoader},
	 * except those in <code>java.*</code>.<br>
	 * This is usually not a big problem as <code>java.*</code> only contains standard library classes,
	 * which can't reference classes outside the standard library.<br>
	 * TODO is this true even for reflection?
	 * <p>
	 * Implementations are recommended to define all classes themselves. If an implementation delegates to the parent classloader,
	 * even if the original classfile is used, all classes referenced by that class will be loaded directly by the parent classloader.
	 * 
	 * @return the defined class, or {@code null} if the class can't be found / shouldn't be found.
	 */
	protected abstract Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL);

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		if(dontTryDefine(name))
			return super.loadClass(name, resolve);

		Class<?> clazz = defineTransformedClassOrThrow(name);
		if(resolve)
			resolveClass(clazz);
		return clazz;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		if(dontTryDefine(name))
			return super.findClass(name);

		return defineTransformedClassOrThrow(name);
	}
	@Override
	protected Class<?> findClass(String moduleName, String name)
	{
		if(dontTryDefine(name))
			return super.findClass(moduleName, name);

		if(moduleName != null)
			//We don't support modules (yet?)
			return null;

		return defineTransformedClassOrNull(name);
	}

	/**
	 * We can't define classes in "java.*". This limitation comes from {@link ClassLoader#preDefineClass}.
	 */
	private boolean dontTryDefine(String name)
	{
		return name.startsWith("java.");
	}

	private Class<?> defineTransformedClassOrThrow(String name) throws ClassFormatError, ClassNotFoundException
	{
		Class<?> result = defineTransformedClassOrNull(name);
		if(result == null)
			throw new ClassNotFoundException(name);

		return result;
	}

	private Class<?> defineTransformedClassOrNull(String name) throws ClassFormatError
	{
		URL originalClassfileURL = getOriginalClassfileOrNull(name);
		byte[] originalClassfile = readBytesOrNull(originalClassfileURL);

		return defineTransformedClass(name, originalClassfile, originalClassfileURL);
	}
	private byte[] readBytesOrNull(URL resource)
	{
		if(resource == null)
			return null;

		try(InputStream in = resource.openStream())
		{
			return in.readAllBytes();
		} catch(IOException e)
		{
			return null;
		}
	}

	private URL getOriginalClassfileOrNull(String name)
	{
		return getResource(resourceNameForClassname(name));
	}

	/**
	 * Copied from {@link jdk.internal.loader.BuiltinClassLoader#findClassOnClassPathOrNull}
	 */
	private String resourceNameForClassname(String name)
	{
		return name.replace('.', '/') + ".class";
	}

	public static TransformingClassLoader of(ClassFileTransformer transformer)
	{
		return new SimpleTransformingClassLoader()
		{
			@Override
			protected byte[] transformClassfile(String name, byte[] originalClassfile, URL originalClassfileURL)
			{
				return transformer.transformClassfile(name, originalClassfile, originalClassfileURL);
			}
		};
	}
	public static TransformingClassLoader of(TransformedClassDefiner definer)
	{
		return new TransformingClassLoader()
		{
			@Override
			protected Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL)
			{
				return definer.defineTransformedClass(name, originalClassfile, originalClassfileURL);
			}
		};
	}

	@FunctionalInterface
	public static interface ClassFileTransformer
	{
		/**
		 * @see TransformingClassLoader#defineTransformedClass(String, byte[], URL)
		 */
		public byte[] transformClassfile(String name, byte[] originalClassfile, URL originalClassfileURL);
	}
	@FunctionalInterface
	public static interface TransformedClassDefiner
	{
		/**
		 * @see TransformingClassLoader#defineTransformedClass(String, byte[], URL)
		 */
		public abstract Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL);
	}
}
