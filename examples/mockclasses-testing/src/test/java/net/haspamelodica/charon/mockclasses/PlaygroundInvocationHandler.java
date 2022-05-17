package net.haspamelodica.charon.mockclasses;

import java.util.Arrays;
import java.util.function.Supplier;

public class PlaygroundInvocationHandler implements DynamicInvocationHandler<String, String, String, String, String>
{
	@Override
	public String createClassContext(ClassInterface classInterface)
	{
		return "class context for " + classInterface.name();
	}

	@Override
	public String createStaticMethodContext(String classContext, MethodInterface methodInterface)
	{
		return "static method context for " + methodInterface.name() + " in " + classContext;
	}

	@Override
	public String createConstructorContext(String classContext, ConstructorInterface constructorInterface)
	{
		return "constructor context for params " + constructorInterface.parameterTypes() + " in " + classContext;
	}

	@Override
	public String createInstanceMethodContext(String classContext, MethodInterface methodInterface)
	{
		//return type needed by invokeInstanceMethod
		return "instance method context for " + methodInterface.name() + " in " + classContext + "; return type " + methodInterface.returnType().getTypeName();
	}

	@Override
	public Object invokeStaticMethod(String classContext, String methodContext, Object[] args)
	{
		System.out.println("Invoking static method; class context " + classContext + "; method context " + methodContext + "; args " + Arrays.toString(args));
		return null;
	}

	@Override
	public String invokeConstructor(String classContext, String constructorContext, Object[] args)
	{
		String instanceContext = "instance context for " + constructorContext + " #" + Math.random();
		System.out.println("Invoking constructor method; class context " + classContext + "; constructor context " + constructorContext
				+ "; args " + Arrays.toString(args) + "; generated instance context is " + instanceContext);
		return instanceContext;
	}

	@Override
	public Object invokeInstanceMethod(String classContext, String methodContext, Object receiver, String receiverContext, Object[] args)
	{
		System.out.println("Invoking static method; class context " + classContext + "; method context " + methodContext
				+ "; receiver " + receiver + "; receiver instance context " + receiverContext + "; args " + Arrays.toString(args));

		// method context ends in return type name
		return switch(methodContext.charAt(methodContext.length() - 1))
		{
			case 'A', 'B' -> ((Supplier<?>) args[0]).get();
			default -> null;
		};
	}
}
