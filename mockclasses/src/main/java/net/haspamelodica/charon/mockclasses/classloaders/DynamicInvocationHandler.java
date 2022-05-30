package net.haspamelodica.charon.mockclasses.classloaders;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;

public interface DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>
{
	public CCTX createClassContext(TypeDefinition type);
	public SCTX createStaticMethodContext(CCTX classContext, MethodDescription method);
	public TCTX createConstructorContext(CCTX classContext, MethodDescription constructor);
	public MCTX createInstanceMethodContext(CCTX classContext, MethodDescription method);
	public default void registerDynamicClassCreated(CCTX classContext, Class<?> clazz)
	{
		// by default, do nothing
	}

	public Object invokeStaticMethod(CCTX classContext, SCTX methodContext, Object[] args);
	public ICTX invokeConstructor(CCTX classContext, TCTX constructorContext, Object receiver, Object[] args);
	public Object invokeInstanceMethod(CCTX classContext, MCTX methodContext, Object receiver, ICTX receiverContext, Object[] args);
}
