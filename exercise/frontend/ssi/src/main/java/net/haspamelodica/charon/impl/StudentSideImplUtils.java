package net.haspamelodica.charon.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.InconsistentHierarchyException;
import net.haspamelodica.charon.marshaling.SerDes;

public class StudentSideImplUtils
{
	public static Method checkReturnAndParameterTypes(Method method, Class<?> expectedReturnType, Class<?>... expectedParameterTypes)
	{
		if(!Arrays.equals(method.getParameterTypes(), expectedParameterTypes))
			throw new FrameworkCausedException("Unexpected parameter types: expected " + expectedParameterTypes + " for " + method);
		if(!method.getReturnType().equals(expectedReturnType))
			throw new FrameworkCausedException("Unknown method of Object: " + method);
		return method;
	}

	public static <K extends Annotation> MethodHandler handlerFor(Method method, Class<K> kindClass,
			StudentSideHandlerGenerator<MethodHandler, K> generateStudentSideHandler)
	{
		return handlerFor(method, kindClass, defaultHandler(method), generateStudentSideHandler);
	}

	public static <R, K extends Annotation> R handlerFor(Method method, Class<K> kindClass, R defaultHandler,
			StudentSideHandlerGenerator<R, K> generateStudentSideHandler)
	{
		K kind = method.getAnnotation(kindClass);

		if(Modifier.isAbstract(method.getModifiers()))
		{
			if(kind == null)
				throw new InconsistentHierarchyException("Method is abstract, but has no student-side meaning: " + method);

			StudentSideName ssn = getStudentSideNameAndIsOverridden(method);
			return generateStudentSideHandler.generate(kind, ssn.studentSideName(), ssn.isOverridden());
		} else
		{
			if(kind != null)
				throw new InconsistentHierarchyException("Method is not abstract, but has a student-side meaning: " + method);

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

	public static List<Class<? extends SerDes<?>>> getSerDeses(AnnotatedElement element)
	{
		// This also catches uses of UseSerDeses
		return Arrays.stream(element.getAnnotationsByType(UseSerDes.class))
				.map((Function<UseSerDes, Class<? extends SerDes<?>>>) UseSerDes::value).toList();
	}

	public static String getStudentSideName(Class<?> clazz)
	{
		return getStudentSideNameAndIsOverridden(clazz).studentSideName();
	}
	public static StudentSideName getStudentSideNameAndIsOverridden(Class<?> clazz)
	{
		return getStudentSideNameAndIsOverridden(clazz, clazzI ->
		{
			Class<?> enclosingClass = clazzI.getEnclosingClass();
			if(enclosingClass == null)
				return clazzI.getName();

			return getStudentSideName(enclosingClass) + '$' + clazzI.getSimpleName();
		});
	}
	public static String getStudentSideName(Method method)
	{
		return getStudentSideNameAndIsOverridden(method).studentSideName();
	}
	public static StudentSideName getStudentSideNameAndIsOverridden(Method method)
	{
		return getStudentSideNameAndIsOverridden(method, Method::getName);
	}
	public static <E extends AnnotatedElement> String getStudentSideName(E element, Function<E, String> getName)
	{
		return getStudentSideNameAndIsOverridden(element, getName).studentSideName();
	}
	public static <E extends AnnotatedElement> StudentSideName getStudentSideNameAndIsOverridden(E element, Function<E, String> getName)
	{
		OverrideStudentSideName overrideStudentSideName = element.getAnnotation(OverrideStudentSideName.class);
		OverrideStudentSideNameByClass overrideStudentSideNameByClass = element.getAnnotation(OverrideStudentSideNameByClass.class);

		if(overrideStudentSideName != null)
			if(overrideStudentSideNameByClass != null)
				throw new InconsistentHierarchyException(element + " is annotated with both "
						+ OverrideStudentSideName.class.getSimpleName() + " and " + OverrideStudentSideNameByClass.class.getSimpleName());
			else
				return new StudentSideName(overrideStudentSideName.value(), true);

		if(overrideStudentSideNameByClass != null)
			//TODO is getCanonicalName correct?
			return new StudentSideName(overrideStudentSideNameByClass.value().getName(), true);

		return new StudentSideName(getName.apply(element), false);
	}

	public static record StudentSideName(String studentSideName, boolean isOverridden)
	{}

	public static void checkNotAnnotatedWith(AnnotatedElement annotatedObject, Class<? extends Annotation> annotationClass)
	{
		if(annotatedObject.isAnnotationPresent(annotationClass))
			throw new InconsistentHierarchyException(annotatedObject + " is unexpectedly annotated with " + annotationClass);
	}

	private StudentSideImplUtils()
	{}
}
