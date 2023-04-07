package net.haspamelodica.charon.mockclasses.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedTyperef;
import net.haspamelodica.charon.marshaling.MarshalingCommunicatorCallbacks;
import net.haspamelodica.charon.marshaling.StudentSideType;
import net.haspamelodica.charon.mockclasses.StudentSideException;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassLoader;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicClassTransformer;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;

public class MockclassesMarshalingTransformer<REF, TYPEREF extends REF>
		implements DynamicClassTransformer, MarshalingCommunicatorCallbacks<REF, TYPEREF, Method, Throwable, StudentSideException>
{
	// can't be final since the classloader references us
	private ClassLoader classloader;

	private final Map<String, Constructor<?>> representationObjectConstructorsByClassname;

	public MockclassesMarshalingTransformer()
	{
		this.representationObjectConstructorsByClassname = new HashMap<>();
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
			representationObjectConstructorsByClassname.put(clazz.getName(), clazz.getConstructor(Object.class));
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
	public Class<?> lookupLocalType(UntranslatedTyperef<REF, TYPEREF> untranslatedTyperef)
	{
		return representationObjectConstructorsByClassname.get(untranslatedTyperef.describe().name()).getDeclaringClass();
	}

	@Override
	public String getCallbackInterfaceCn(Object exerciseSideObject)
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public CallbackMethod<Method> lookupCallbackInstanceMethod(StudentSideType<TYPEREF, ?> receiverStaticType, Class<?> receiverDynamicType, String name, StudentSideType<TYPEREF, ?> returnType, List<StudentSideType<TYPEREF, ?>> params)
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public Object callCallbackInstanceMethodChecked(Method methodData, Object receiver, List<Object> args) throws ExceptionInTargetException
	{
		throw new UnsupportedOperationException("The Mockclasses frontend does not support callbacks.");
	}

	@Override
	public Object createRepresentationObject(StudentSideType<TYPEREF, ?> type, UntranslatedRef<REF, TYPEREF> untranslatedRef)
	{
		try
		{
			return representationObjectConstructorsByClassname.get(type.studentSideCN()).newInstance(new Object[] {untranslatedRef.ref()});
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
	public StudentSideException newStudentCausedException(Throwable studentSideThrowable)
	{
		return new StudentSideException(studentSideThrowable);
	}
}
