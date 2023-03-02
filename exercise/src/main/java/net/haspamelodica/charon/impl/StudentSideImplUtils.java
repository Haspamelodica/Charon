package net.haspamelodica.charon.impl;

import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.marshaling.SerDes;

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

			return generateStudentSideHandler.generate(kind, getStudentSideName(method), false);
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

	public interface StudentSideHandlerGenerator<R, K>
	{
		public R generate(K kind, String name, boolean nameOverridden);
	}

	public static <P> P createProxyInstance(Class<P> proxiedClass, InvocationHandler handler)
	{
		Object proxyInstance = Proxy.newProxyInstance(proxiedClass.getClassLoader(), new Class[] {proxiedClass}, handler);
		@SuppressWarnings("unchecked")
		P proxyInstanceCasted = (P) proxyInstance;
		return proxyInstanceCasted;
	}

	public static List<Object> argsToList(Object[] args)
	{
		return args == null ? List.of() : Arrays.asList(args);
	}

	public static List<Class<? extends SerDes<?>>> getSerDeses(AnnotatedElement element)
	{
		// This also catches uses of UseSerDeses
		return Arrays.stream(element.getAnnotationsByType(UseSerDes.class))
				.map((Function<UseSerDes, Class<? extends SerDes<?>>>) UseSerDes::value).toList();
	}

	public static List<StudentSideType<?>> mapToStudentSide(Class<?>[] classes)
	{
		return mapToStudentSide(Arrays.stream(classes)).toList();
	}
	public static List<StudentSideType<?>> mapToStudentSide(List<Class<?>> classes)
	{
		return mapToStudentSide(classes.stream()).toList();
	}
	public static Stream<StudentSideType<?>> mapToStudentSide(Stream<Class<?>> classes)
	{
		return classes.map(StudentSideImplUtils::mapToStudentSide);
	}
	public static <T> StudentSideType<T> mapToStudentSide(Class<T> clazz)
	{
		return new StudentSideType<>(clazz, mapNameToStudentSide(clazz));
	}
	public static String mapNameToStudentSide(Class<?> clazz)
	{
		//TODO not pretty
		if(StudentSideInstance.class.isAssignableFrom(clazz))
			return getStudentSideName(clazz);
		return classToName(clazz);
	}

	public static record StudentSideType<T>(Class<T> localType, String studentSideCN)
	{}

	public static String getStudentSideName(Class<?> clazz)
	{
		return getStudentSideName(clazz, Class::getName);
	}
	public static String getStudentSideName(Method method)
	{
		return getStudentSideName(method, Method::getName);
	}
	public static <E extends AnnotatedElement> String getStudentSideName(E element, Function<E, String> getName)
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
