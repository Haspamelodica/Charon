package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.marshaling.RepresentationObjectMarshaler;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassTransformer;

public class MockclassesMarshalingTransformer implements DynamicClassTransformer, RepresentationObjectMarshaler
{
	// can't be final since the classloader references us
	private ClassLoader classloader;

	private final Map<String, Constructor<?>> defaultConstructorsByClassname;

	public MockclassesMarshalingTransformer()
	{
		this.defaultConstructorsByClassname = new HashMap<>();
	}

	public void setClassloader(ClassLoader classloader)
	{
		this.classloader = classloader;
	}
	public ClassLoader getClassloader()
	{
		return classloader;
	}

	public void registerDynamicClass(Class<?> clazz)
	{
		try
		{
			defaultConstructorsByClassname.put(clazz.getName(), clazz.getConstructor());
		} catch(NoSuchMethodException e)
		{
			throw new RuntimeException("Generated constructor can't be found", e);
		}
	}

	@Override
	public Builder<?> transform(Builder<?> builder, ElementMatcher<FieldDescription> instanceContextFieldMatcher,
			FieldNameExtractor instanceContextFieldNameExtractor)
	{
		return builder
				// Define constructor. Parameter declared as Object to avoid having two Ref classes;
				// and the field is declared as Object either way.
				.defineConstructor(Visibility.PUBLIC)
				.intercept(MethodCall.invoke(DynamicClassLoader.Object_new));
	}

	@Override
	public Object createRepresentationObject(UntranslatedRef untranslatedRef)
	{
		try
		{
			String cn = untranslatedRef.getClassname();
			return defaultConstructorsByClassname.get(cn).newInstance();
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Error invoking generated constructor", e);
		}
	}
}
