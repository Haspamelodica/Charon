package net.haspamelodica.charon.mockclasses;

import java.util.Arrays;
import java.util.List;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeList;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.marshaling.Marshaler;
import net.haspamelodica.charon.mockclasses.dynamicclasses.DynamicInvocationHandler;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.refs.Ref;

public class MockclassesInvocationHandler<REF extends Ref<?, Object>>
		implements DynamicInvocationHandler<String, MethodDescription, MethodDescription, MethodDescription, REF>
{
	private final StudentSideCommunicatorClientSide<REF>	communicator;
	private final Marshaler<?, ?, REF>						marshaler;
	private final MockclassesMarshalingTransformer<REF>		transformer;

	public MockclassesInvocationHandler(StudentSideCommunicatorClientSide<REF> communicator, Marshaler<?, ?, REF> marshaler, MockclassesMarshalingTransformer<REF> transformer)
	{
		this.communicator = communicator;
		this.marshaler = marshaler;
		this.transformer = transformer;
	}

	@Override
	public String createClassContext(TypeDefinition typeDefinition)
	{
		return typeDefinition.getActualName();
	}
	@Override
	public MethodDescription createStaticMethodContext(String classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public MethodDescription createConstructorContext(String classContext, MethodDescription constructor)
	{
		return constructor;
	}
	@Override
	public MethodDescription createInstanceMethodContext(String classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public void registerDynamicClassCreated(String classContext, Class<?> clazz)
	{
		transformer.registerDynamicClass(clazz);
	}

	@Override
	public Object invokeStaticMethod(String classContext, MethodDescription methodContext, Object[] args)
	{
		TypeList.Generic parameterTypes = methodContext.getParameters().asTypeList();
		List<REF> argsRefs = marshaler.send(toClasses(parameterTypes), Arrays.asList(args));

		REF result = communicator.callStaticMethod(
				classContext,
				methodContext.getActualName(),
				toClassname(methodContext.getReturnType().asErasure()),
				toClassnames(methodContext.getParameters().asTypeList()),
				argsRefs);

		return marshaler.receive(toClass(methodContext.getReturnType()), result);
	}
	@Override
	public REF invokeConstructor(String classContext, MethodDescription constructorContext, Object receiver, Object[] args)
	{
		TypeList.Generic parameterTypes = constructorContext.getParameters().asTypeList();
		List<REF> argsRefs = marshaler.send(toClasses(parameterTypes), Arrays.asList(args));

		REF result = communicator.callConstructor(
				classContext,
				toClassnames(parameterTypes),
				argsRefs);

		result.setReferrer(receiver);

		return result;
	}
	@Override
	public Object invokeInstanceMethod(String classContext, MethodDescription methodContext, Object receiver, REF receiverContext, Object[] args)
	{
		TypeList.Generic parameterTypes = methodContext.getParameters().asTypeList();
		List<REF> argsRefs = marshaler.send(toClasses(parameterTypes), Arrays.asList(args));

		REF result = communicator.callInstanceMethod(
				classContext,
				methodContext.getActualName(),
				toClassname(methodContext.getReturnType()),
				toClassnames(parameterTypes),
				receiverContext, argsRefs);

		return marshaler.receive(toClass(methodContext.getReturnType()), result);
	}

	private List<? extends Class<?>> toClasses(List<? extends TypeDefinition> typeDefinitions)
	{
		return typeDefinitions.stream().map(this::toClass).toList();
	}
	private static List<String> toClassnames(List<? extends TypeDefinition> typeDefinitions)
	{
		return typeDefinitions.stream().map(MockclassesInvocationHandler::toClassname).toList();
	}
	private Class<?> toClass(TypeDefinition typeDefinition)
	{
		return ReflectionUtils.nameToClass(toClassname(typeDefinition), transformer.getClassloader());
	}
	private static String toClassname(TypeDefinition typeDefinition)
	{
		return typeDefinition.asErasure().getActualName();
	}
}
