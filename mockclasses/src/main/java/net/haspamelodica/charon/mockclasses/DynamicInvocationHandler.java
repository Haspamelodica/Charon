package net.haspamelodica.charon.mockclasses;

public interface DynamicInvocationHandler<CCTX, MCTX, SCTX, TCTX, ICTX>
{
	public CCTX createClassContext(ClassInterface classInterface);
	public SCTX createStaticMethodContext(CCTX classContext, MethodInterface methodInterface);
	public TCTX createConstructorContext(CCTX classContext, ConstructorInterface constructorInterface);
	public MCTX createInstanceMethodContext(CCTX classContext, MethodInterface methodInterface);

	public Object invokeStaticMethod(CCTX classContext, SCTX methodContext, Object[] args);
	public ICTX invokeConstructor(CCTX classContext, TCTX constructorContext, Object[] args);
	public Object invokeInstanceMethod(CCTX classContext, MCTX methodContext, Object receiver, ICTX receiverContext, Object[] args);
}
