package net.haspamelodica.charon.mockclasses.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.haspamelodica.charon.impl.StudentSideImplUtils.StudentSideType;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInvocationHandler;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class MockclassesInvocationHandler
		implements DynamicInvocationHandler<StudentSideType<?>, MethodDescription, MethodDescription, MethodDescription, Object>
{
	private final MarshalingCommunicator<?>			marshalingCommunicator;
	private final MockclassesMarshalingTransformer	transformer;

	public MockclassesInvocationHandler(MarshalingCommunicator<?> marshalingCommunicator, MockclassesMarshalingTransformer transformer)
	{
		this.marshalingCommunicator = marshalingCommunicator;
		this.transformer = transformer;
	}

	@Override
	public StudentSideType<?> createClassContext(TypeDefinition typeDefinition)
	{
		return toStudentSideType(typeDefinition);
	}

	@Override
	public MethodDescription createStaticMethodContext(StudentSideType<?> classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public MethodDescription createConstructorContext(StudentSideType<?> classContext, MethodDescription constructor)
	{
		return constructor;
	}
	@Override
	public MethodDescription createInstanceMethodContext(StudentSideType<?> classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public void registerDynamicClassCreated(StudentSideType<?> classContext, Class<?> clazz)
	{
		transformer.registerDynamicClass(clazz);
	}

	@Override
	public Object invokeStaticMethod(StudentSideType<?> classContext, MethodDescription methodContext, Object[] args)
	{
		return marshalingCommunicator.callStaticMethod(
				classContext,
				methodContext.getActualName(),
				toStudentSideType(methodContext.getReturnType().asErasure()),
				toStudentSideTypes(methodContext.getParameters().asTypeList()),
				Arrays.asList(args));
	}
	@Override
	public Object invokeConstructor(StudentSideType<?> classContext, MethodDescription constructorContext, Object receiver, Object[] args)
	{
		Object result = marshalingCommunicator.callConstructor(
				classContext,
				toStudentSideTypes(constructorContext.getParameters().asTypeList()),
				Arrays.asList(args));

		return result;
	}
	@Override
	public Object invokeInstanceMethod(StudentSideType<?> classContext, MethodDescription methodContext, Object receiver, Object receiverContext, Object[] args)
	{
		return marshalingCommunicator.callInstanceMethod(
				classContext,
				methodContext.getActualName(),
				toStudentSideType(methodContext.getReturnType()),
				toStudentSideTypes(methodContext.getParameters().asTypeList()),
				receiverContext, Arrays.asList(args));
	}

	private List<StudentSideType<?>> toStudentSideTypes(List<? extends TypeDefinition> typeDefinitions)
	{
		Stream<StudentSideType<?>> stream = typeDefinitions.stream().map(this::toStudentSideType);
		return stream.toList();
	}
	private StudentSideType<?> toStudentSideType(TypeDefinition typeDefinition)
	{
		return new StudentSideType<>(toClass(typeDefinition), typeDefinition.getActualName());
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
