package net.haspamelodica.charon.mockclasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.marshaling.RepresentationObjectMarshaler;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicClassTransformer;
import net.haspamelodica.charon.refs.Ref;

public class MockclassesMarshalingTransformer<REF extends Ref<?, Object>> implements DynamicClassTransformer,
		RepresentationObjectMarshaler<Object, Mockclass<REF>, REF>
{
	private final StudentSideCommunicator<REF> communicator;
	// can't be final since the classloader references us
	private ClassLoader classloader;

	private final Map<String, Constructor<? extends Mockclass<REF>>> refBasedConstructorsByClassname;

	public MockclassesMarshalingTransformer(StudentSideCommunicator<REF> communicator)
	{
		this.communicator = communicator;
		this.refBasedConstructorsByClassname = new HashMap<>();
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
		@SuppressWarnings("unchecked") // Same reason as in representationObjectClass.
		Class<? extends Mockclass<REF>> clazzCasted = (Class<? extends Mockclass<REF>>) clazz;
		try
		{
			refBasedConstructorsByClassname.put(clazz.getName(), clazzCasted.getConstructor(Object.class));
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
				// Implement Mockclass.
				// Yes, we pass the Mockclass class of the "outer" classloader, but that's fine
				// since Mockclass mustn't be overridden anyway.
				.implement(Mockclass.class)

				// Create Ref getter from Mockclass. Dynamic typing because the field is declared as Object.
				.method(ElementMatchers.named(Mockclass.GET_REF_METHOD_NAME))
				.intercept(FieldAccessor.of(instanceContextFieldNameExtractor).withAssigner(Assigner.DEFAULT, Typing.DYNAMIC))

				// Define constructor. Parameter declared as Object to avoid having two Ref classes;
				// and the field is declared as Object either way.
				.defineConstructor(Visibility.PUBLIC)
				.withParameter(Object.class)
				.intercept(MethodCall.invoke(DynamicClassLoader.Object_new).andThen(
						FieldAccessor.of(instanceContextFieldNameExtractor).setsArgumentAt(0)));
	}

	@Override
	public Class<Mockclass<REF>> representationObjectClass()
	{
		try
		{
			// Users are responsible to not mix mockclasses created for different Refs.
			// So, it's fine to return a Class which isn't "wildcarded". (It only makes a compile-time difference anyway.)
			@SuppressWarnings("unchecked")
			Class<Mockclass<REF>> clazz = (Class<Mockclass<REF>>) Class.forName(Mockclass.class.getName(), true, classloader);
			return clazz;
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public REF marshal(Mockclass<REF> obj)
	{
		return obj.getRef();
	}

	@Override
	public Mockclass<REF> unmarshal(REF objRef)
	{
		try
		{
			String cn = communicator.getStudentSideClassname(objRef);
			Mockclass<REF> mock = refBasedConstructorsByClassname.get(cn).newInstance(objRef);
			objRef.setReferrer(mock);
			return mock;
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Error invoking generated constructor", e);
		}
	}
}
