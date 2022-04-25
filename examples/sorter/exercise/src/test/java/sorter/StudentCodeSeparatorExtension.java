package sorter;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import net.haspamelodica.studentcodeseparator.ExerciseSideRunner;
import net.haspamelodica.studentcodeseparator.StudentSide;
import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.exceptions.InconsistentHierarchyException;
import net.haspamelodica.studentcodeseparator.exceptions.MissingSerializerException;

/**
 * A JUnit 5 {@link ParameterResolver} extension making an instance of {@link StudentSide} accessible to test code.
 * Currently, the extension reconnects with the student side each time the
 */
public class StudentCodeSeparatorExtension extends TypeBasedParameterResolver<StudentSide> implements InvocationInterceptor
{
	private final StudentSide redirectingStudentSide = new StudentSide()
	{
		@Override
		public <SI extends StudentSideInstance, SP extends StudentSidePrototype<SI>> SP createPrototype(Class<SP> prototypeClass)
				throws InconsistentHierarchyException, MissingSerializerException
		{
			return currentRealStudentSide.createPrototype(prototypeClass);
		}
	};

	private StudentSide currentRealStudentSide;

	@Override
	public StudentSide resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
	{
		return redirectingStudentSide;
	}

	//TODO also support class constructors
//	@Override
//	public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable
//	{
//		return invokeWithStudentSide(invocation);
//	}
	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		return invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}
	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable
	{
		invokeWithStudentSide(invocation);
	}

	public <R> R invokeWithStudentSide(Invocation<R> invocation) throws IOException, InterruptedException, Throwable
	{
		System.out.println("invokeWithStudentSide");
		new Exception().printStackTrace(System.out);
		@SuppressWarnings("unchecked")
		R[] result = (R[]) new Object[1];

		ExerciseSideRunner.run(realStudentSide ->
		{
			currentRealStudentSide = realStudentSide;
			result[0] = invocation.proceed();
			currentRealStudentSide = null;
		}, StudentCodeSeparatorExtension.class, System.getProperty("studentcodeseparator", "").split(" "));
		return result[0];
	}
}
