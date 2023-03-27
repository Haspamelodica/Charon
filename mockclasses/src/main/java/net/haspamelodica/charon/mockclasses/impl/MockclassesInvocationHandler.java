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

public class MockclassesInvocationHandler<REF>
		implements DynamicInvocationHandler<TypeDefinition, MethodDescription, MethodDescription, MethodDescription, REF>
{
	private final MarshalingCommunicator<REF>			marshalingCommunicator;
	private final MockclassesMarshalingTransformer<REF>	transformer;

	public MockclassesInvocationHandler(MarshalingCommunicator<REF> marshalingCommunicator, MockclassesMarshalingTransformer<REF> transformer)
	{
		this.marshalingCommunicator = marshalingCommunicator;
		this.transformer = transformer;
	}

	@Override
	public TypeDefinition createClassContext(TypeDefinition typeDefinition)
	{
		// We can't use toStudentSideType here: We are called when the class described by typeDefinition is about to be created,
		// but toStudentSideType requires the class to already exist.
		return typeDefinition;
	}

	@Override
	public MethodDescription createStaticMethodContext(TypeDefinition classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public MethodDescription createConstructorContext(TypeDefinition classContext, MethodDescription constructor)
	{
		return constructor;
	}
	@Override
	public MethodDescription createInstanceMethodContext(TypeDefinition classContext, MethodDescription method)
	{
		return method;
	}
	@Override
	public void registerDynamicClassCreated(TypeDefinition classContext, Class<?> clazz)
	{
		transformer.registerDynamicClass(clazz);
	}

	@Override
	public Object invokeStaticMethod(TypeDefinition classContext, MethodDescription methodContext, Object[] args) throws Throwable
	{
		return marshalingCommunicator.callStaticMethod(
				toStudentSideType(classContext),
				methodContext.getActualName(),
				toStudentSideType(methodContext.getReturnType().asErasure()),
				toStudentSideTypes(methodContext.getParameters().asTypeList()),
				Arrays.asList(args));
	}
	@Override
	public REF invokeConstructor(TypeDefinition classContext, MethodDescription constructorContext, Object receiver, Object[] args) throws Throwable
	{
		return marshalingCommunicator.callConstructorExistingRepresentationObject(
				toStudentSideType(classContext),
				toStudentSideTypes(constructorContext.getParameters().asTypeList()),
				Arrays.asList(args),
				receiver);
	}
	@Override
	public Object invokeInstanceMethod(TypeDefinition classContext, MethodDescription methodContext, Object receiver, REF receiverContext,
			Object[] args) throws Throwable
	{
		return marshalingCommunicator.callInstanceMethodRawReceiver(
				toStudentSideType(classContext),
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
