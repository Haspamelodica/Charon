package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.mockclasses.StudentSideException;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassTransformer;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.util.LazyValue;

public class MockclassesMarshalingTransformer<REF, TYPEREF extends REF>
		implements DynamicClassTransformer, MarshalingCommunicatorCallbacks<REF, TYPEREF, Method, Throwable, StudentSideException>
{
	// can't be final since the classloader references us
	private ClassLoader classloader;
	// can't be final since this references the classloader, which references us
	private LazyValue<MarshalingCommunicator<REF, TYPEREF, StudentSideException>> communicator;

	private final Map<String, Constructor<?>> representationObjectConstructorsByClassname;

	public MockclassesMarshalingTransformer()
	{
		this.representationObjectConstructorsByClassname = new HashMap<>();
	}

	public void setClassloaderAndCommunicator(ClassLoader classloader, LazyValue<MarshalingCommunicator<REF, TYPEREF, StudentSideException>> communicator)
	{
		this.classloader = classloader;
		this.communicator = communicator;
	}
	public ClassLoader getClassloader()
	{
		return classloader;
	}

	public void registerDynamicClass(Class<?> clazz)
	{
		try
		{
			representationObjectConstructorsByClassname.put(ReflectionUtils.classToName(clazz), clazz.getConstructor(Object.class));
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
				// Define constructor. Parameter declared as Object to avoid having to find out which class REF is;
				// and the field is declared as Object either way.
				.defineConstructor(Visibility.PUBLIC)
				.withParameter(Object.class)
				.intercept(MethodCall.invoke(DynamicClassLoader.Object_new)
						.andThen(FieldAccessor.of(instanceContextFieldNameExtractor).setsArgumentAt(0)));
	}

	@Override
	public TYPEREF lookupCorrespondingStudentSideTypeForRepresentationClass(Class<?> representationClass, boolean throwIfNotFound)
	{
		return Objects.requireNonNull(communicator.get().getTypeByNameAndVerify(ReflectionUtils.classToName(representationClass)));
	}

	@Override
	public String getCallbackInterfaceCn(Object exerciseSideObject)
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public CallbackMethod<Method> lookupCallbackInstanceMethod(TYPEREF receiverStaticTyperef, String name, TYPEREF returnTyperef, List<TYPEREF> paramTyperefs,
			Class<?> receiverDynamicRepresentationType)
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public CallbackOperationOutcome<Object, Throwable> callCallbackInstanceMethodChecked(Method methodData, Object receiver, List<Object> args)
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public Object createRepresentationObject(UntranslatedRef<REF, TYPEREF> untranslatedRef)
	{
		try
		{
			Constructor<?> constructor = representationObjectConstructorsByClassname.get(untranslatedRef.getType().describe().name());
			if(constructor == null)
				// We should not try to blindly load the class with that name since that could be dangerous.
				//TODO introduce mechanism to let the Mockclasses classloader try to load a class by name, but only if the name refers to a mockclass.
				throw new FrameworkCausedException("Student tried creating a class which hasn't been seen before; this isn't yet supported."
						+ " As a workaround, load all mockclasses beforehand (for example by callling Mockclass.class.getModule();"
						+ " getModule because that method is very cheap).");
			return constructor.newInstance(new Object[] {untranslatedRef.ref()});
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new RuntimeException("Error invoking generated constructor", e);
		}
	}

	@Override
	public Throwable checkRepresentsStudentSideThrowableAndCastOrNull(Object representationObject)
	{
		return representationObject instanceof Throwable throwable ? throwable : null;
	}

	@Override
	public StudentSideException createStudentCausedException(Throwable studentSideThrowable)
	{
		return new StudentSideException(studentSideThrowable);
	}
}
