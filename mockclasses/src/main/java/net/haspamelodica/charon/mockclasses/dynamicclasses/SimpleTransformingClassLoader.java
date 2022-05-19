package net.haspamelodica.charon.mockclasses.dynamicclasses;

import java.net.URL;

public abstract class SimpleTransformingClassLoader extends TransformingClassLoader
{
	/**
	 * @see TransformingClassLoader#defineTransformedClass(String, byte[], URL)
	 */
	protected abstract byte[] transformClassfile(String name, byte[] original, URL classfileResource);

	@Override
	protected Class<?> defineTransformedClass(String name, byte[] originalClassfile, URL originalClassfileURL)
	{
		byte[] transformedClassfile = transformClassfile(name, originalClassfile, originalClassfileURL);

		if(transformedClassfile == null)
			return null;

		//This might result in a SecurityException; for example for sealed packages. Ignore to implicitly rethrow.
		return defineClass(name, transformedClassfile, 0, transformedClassfile.length);
	}

	@FunctionalInterface
	public static interface ClassFileTransformer
	{
		/**
		 * @see TransformingClassLoader#defineTransformedClass(String, byte[], URL)
		 */
		public byte[] transformClassfile(String name, byte[] originalClassfile, URL originalClassfileURL);
	}
}
