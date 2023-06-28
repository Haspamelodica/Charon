package net.haspamelodica.charon.mockclasses.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.haspamelodica.charon.marshaling.MarshalingCommunicator;
import net.haspamelodica.charon.mockclasses.StudentSideException;
import net.haspamelodica.charon.mockclasses.classloaders.DynamicInvocationHandler;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class MockclassesInvocationHandler<REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
		implements DynamicInvocationHandler<TypeDefinition, MethodDescription, MethodDescription, MethodDescription, REF>
{
	private final MarshalingCommunicator<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, StudentSideException>	marshalingCommunicator;
	private final MockclassesMarshalingTransformer<REF, TYPEREF>													transformer;

	public MockclassesInvocationHandler(
			MarshalingCommunicator<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, StudentSideException> marshalingCommunicator,
			MockclassesMarshalingTransformer<REF, TYPEREF> transformer)
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
	public Object invokeStaticMethod(TypeDefinition classContext, MethodDescription methodContext, Object[] args) throws StudentSideException
	{
		Class<?> returnType = toClass(methodContext.getReturnType().asErasure());
		List<Class<?>> params = toClasses(methodContext.getParameters().asTypeList());
		//TODO cache methodref
		METHODREF methodref = marshalingCommunicator.lookupMethod(
				toClass(classContext), methodContext.getActualName(), returnType, params, true);
		return marshalingCommunicator.callStaticMethod(methodref, returnType, params, Arrays.asList(args));
	}
	@Override
	public REF invokeConstructor(TypeDefinition classContext, MethodDescription constructorContext, Object receiver, Object[] args) throws StudentSideException
	{
		List<Class<?>> params = toClasses(constructorContext.getParameters().asTypeList());
		//TODO cache constructorref
		CONSTRUCTORREF constructorref = marshalingCommunicator.lookupConstructor(toClass(classContext), params);
		return marshalingCommunicator.callConstructorExistingRepresentationObject(constructorref, params, Arrays.asList(args), receiver);
	}
	@Override
	public Object invokeInstanceMethod(TypeDefinition classContext, MethodDescription methodContext, Object receiver, REF receiverContext,
			Object[] args) throws StudentSideException
	{
		Class<?> returnType = toClass(methodContext.getReturnType());
		List<Class<?>> params = toClasses(methodContext.getParameters().asTypeList());
		//TODO cache methodref
		METHODREF methodref = marshalingCommunicator.lookupMethod(toClass(classContext), methodContext.getActualName(), returnType, params, false);
		return marshalingCommunicator.callInstanceMethodRawReceiver(methodref, returnType, params, receiverContext, Arrays.asList(args));
	}

	private List<Class<?>> toClasses(List<? extends TypeDefinition> typeDefinitions)
	{
		Stream<Class<?>> stream = typeDefinitions.stream().map(this::toClass);
		return stream.toList();
	}
	private Class<?> toClass(TypeDefinition typeDefinition)
	{
		try
		{
			return ReflectionUtils.nameToClass(toClassname(typeDefinition), transformer.getClassloader());
		} catch(ClassNotFoundException e)
		{
			//TODO better exception type
			throw new RuntimeException(e);
		}
	}
	private static String toClassname(TypeDefinition typeDefinition)
	{
		return typeDefinition.asErasure().getActualName();
	}
}
