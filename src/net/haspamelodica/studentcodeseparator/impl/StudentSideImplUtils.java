package net.haspamelodica.studentcodeseparator.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.UseSerializer;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;
import net.haspamelodica.studentcodeseparator.serialization.Serializer;

public class StudentSideImplUtils
{
	public static <K extends Annotation> MethodHandler
			handlerFor(Method method, Class<K> kindClass, StudentSideHandlerGenerator<MethodHandler, K> generateStudentSideHandler)
	{
		return handlerFor(method, kindClass, defaultHandler(method), generateStudentSideHandler);
	}

	public static <R, K extends Annotation> R handlerFor(Method method, Class<K> kindClass,
			R defaultHandler, StudentSideHandlerGenerator<R, K> generateStudentSideHandler)
	{
		K kind = method.getAnnotation(kindClass);
		if(Modifier.isAbstract(method.getModifiers()))
		{
			if(kind == null)
				throw new InconsistentHierarchyException("Method is abstract, but has no special student-side meaning: " + method);

			return generateStudentSideHandler.generate(kind, getName(method), false);
		} else
		{
			if(kind != null)
				throw new InconsistentHierarchyException("Method is not abstract, but has a special student-side meaning: " + method);

			return defaultHandler;
		}
	}

	public static MethodHandler defaultHandler(Method method)
	{
		return (proxy, args) -> InvocationHandler.invokeDefault(proxy, method, args);
	}

	public static <REF> InstanceMethodHandler<REF> defaultInstanceHandler(Method method)
	{
		return (ref, proxy, args) -> InvocationHandler.invokeDefault(proxy, method, args);
	}

	public interface StudentSideHandlerGenerator<R, K>
	{
		public R generate(K kind, String name, boolean nameOverridden);
	}

	public static <P> P createProxyInstance(Class<P> proxiedClass, InvocationHandler handler)
	{
		Object proxyInstance = Proxy.newProxyInstance(proxiedClass.getClassLoader(), new Class[] {proxiedClass},
				(proxy, method, args) -> handler.invoke(proxy, method, args));
		@SuppressWarnings("unchecked")
		P proxyInstanceCasted = (P) proxyInstance;
		return proxyInstanceCasted;
	}

	public static List<Object> argsToList(Object[] args)
	{
		return args == null ? List.of() : Arrays.asList(args);
	}

	public static List<Class<? extends Serializer<?>>> getSerializers(AnnotatedElement element)
	{
		//This also catches uses of UseSerializers
		return Arrays.stream(element.getAnnotationsByType(UseSerializer.class))
				.map((Function<UseSerializer, Class<? extends Serializer<?>>>) UseSerializer::value).toList();
	}

	public static String getName(Class<?> clazz)
	{
		return getName(clazz, Class::getName);
	}
	public static String getName(Method method)
	{
		return getName(method, Method::getName);
	}
	public static <E extends AnnotatedElement> String getName(E element, Function<E, String> getName)
	{
		OverrideStudentSideName overrideStudentSideName = element.getAnnotation(OverrideStudentSideName.class);
		if(overrideStudentSideName != null)
			return overrideStudentSideName.value();

		return getName.apply(element);
	}

	public static void checkNotAnnotatedWith(AnnotatedElement annotatedObject, Class<? extends Annotation> annotationClass)
	{
		if(annotatedObject.isAnnotationPresent(annotationClass))
			throw new InconsistentHierarchyException(annotatedObject + " is unexpectedly annotated with " + annotationClass);
	}

	private StudentSideImplUtils()
	{}
}
