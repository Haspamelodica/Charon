package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClassWrapReflectiveAction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.HiddenCallbackErrorException;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class DirectSameJVMCommunicator<TC extends Transceiver>
		implements StudentSideCommunicator<Object, Class<?>, TC, InternalCallbackManager<Object>>, InternalCallbackManager<Object>
{
	private final StudentSideCommunicatorCallbacks<Object, Class<?>> callbacks;

	private final TC transceiver;

	public DirectSameJVMCommunicator(StudentSideCommunicatorCallbacks<Object, Class<?>> callbacks,
			Function<StudentSideCommunicatorCallbacks<Object, Class<?>>, TC> createTransceiver)
	{
		this.callbacks = callbacks;
		this.transceiver = createTransceiver.apply(this.callbacks);
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return true;
	}

	@Override
	public OperationOutcome<Object, Class<?>> getTypeByName(String typeName)
	{
		return nameToClassWrapReflectiveAction(typeName);
	}

	@Override
	public Class<?> getArrayType(Class<?> componentType)
	{
		return componentType.arrayType();
	}

	@Override
	public Class<?> getTypeOf(Object ref)
	{
		return ref.getClass();
	}

	@Override
	public StudentSideTypeDescription<Class<?>> describeType(Class<?> type)
	{
		return new StudentSideTypeDescription<>(
				type.isPrimitive() ? Kind.PRIMITIVE : type.isArray() ? Kind.ARRAY : type.isInterface() ? Kind.INTERFACE : Kind.CLASS,
				classToName(type),
				Optional.ofNullable(type.getSuperclass()),
				List.of(type.getInterfaces()),
				Optional.ofNullable(type.getComponentType()));
	}

	@Override
	public Class<?> getTypeHandledBySerdes(Object serdesRef)
	{
		// responsibility of caller
		SerDes<?> serdes = (SerDes<?>) serdesRef;

		return serdes.getHandledClass();
	}

	@Override
	public OperationOutcome<Object, Class<?>> newArray(Class<?> arrayType, int length)
	{
		return ReflectionUtils.newArray(arrayType, length);
	}

	@Override
	public OperationOutcome<Object, Class<?>> newMultiArray(Class<?> arrayType, List<Integer> dimensions)
	{
		return ReflectionUtils.newMultiArray(arrayType, dimensions);
	}

	@Override
	public OperationOutcome<Object, Class<?>> newArrayWithInitialValues(Class<?> arrayType, List<Object> initialValues)
	{
		return ReflectionUtils.newArrayWithInitialValues(arrayType, initialValues);
	}

	@Override
	public int getArrayLength(Object arrayRef)
	{
		return ReflectionUtils.getArrayLength(arrayRef);
	}

	@Override
	public OperationOutcome<Object, Class<?>> getArrayElement(Object arrayRef, int index)
	{
		return ReflectionUtils.getArrayElement(arrayRef, index);
	}

	@Override
	public OperationOutcome<Void, Class<?>> setArrayElement(Object arrayRef, int index, Object valueRef)
	{
		return ReflectionUtils.setArrayElement(arrayRef, index, valueRef);
	}

	@Override
	public OperationOutcome<Object, Class<?>> callConstructor(Class<?> type, List<Class<?>> params, List<Object> argRefs)
	{
		return ReflectionUtils.callConstructor(type, params, argRefs);
	}

	@Override
	public OperationOutcome<Object, Class<?>> callStaticMethod(Class<?> type, String name, Class<?> returnType, List<Class<?>> params,
			List<Object> argRefs)
	{
		return ReflectionUtils.callStaticMethod(type, name, returnType, params, argRefs);
	}

	@Override
	public OperationOutcome<Object, Class<?>> getStaticField(Class<?> type, String name, Class<?> fieldType)
	{
		return ReflectionUtils.getStaticField(type, name, fieldType);
	}

	@Override
	public OperationOutcome<Void, Class<?>> setStaticField(Class<?> type, String name, Class<?> fieldType, Object valueRef)
	{
		return setStaticField_(type, name, fieldType, valueRef);
	}
	// extracted to method so the cast is expressible in Java
	private <F> OperationOutcome<Void, Class<?>> setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		return ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public OperationOutcome<Object, Class<?>> callInstanceMethod(Class<?> type, String name, Class<?> returnType, List<Class<?>> params,
			Object receiverRef, List<Object> argRefs)
	{
		return callInstanceMethod_(type, name, returnType, params, receiverRef, argRefs);
	}
	// extracted to method so the cast is expressible in Java
	private <T> OperationOutcome<Object, Class<?>> callInstanceMethod_(Class<T> clazz, String name, Class<?> returnClass, List<Class<?>> params,
			Object receiver, List<Object> args)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.callInstanceMethod(clazz, name, returnClass, params, receiverCasted, args);
	}

	@Override
	public OperationOutcome<Object, Class<?>> getInstanceField(Class<?> type, String name, Class<?> fieldType, Object receiverRef)
	{
		return getInstanceField_(type, name, fieldType, receiverRef);
	}
	// extracted to method so the cast is expressible in Java
	private <T> OperationOutcome<Object, Class<?>> getInstanceField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getInstanceField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public OperationOutcome<Void, Class<?>> setInstanceField(Class<?> type, String name, Class<?> fieldType, Object receiverRef, Object valueRef)
	{
		return setInstanceField_(type, name, fieldType, receiverRef, valueRef);
	}
	// extracted to method so the casts are expressible in Java
	private <T, F> OperationOutcome<Void, Class<?>> setInstanceField_(Class<T> clazz, String name, Class<F> fieldClass, Object receiver, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		return ReflectionUtils.setInstanceField(clazz, name, fieldClass, receiverCasted, valueCasted);
	}

	@Override
	public TC getTransceiver()
	{
		return transceiver;
	}

	@Override
	public Object createCallbackInstance(String interfaceCn)
	{
		OperationOutcome<Object, Class<?>> interfaceTypeReflectiveOperationOutcome = nameToClassWrapReflectiveAction(interfaceCn);
		if(!(interfaceTypeReflectiveOperationOutcome instanceof OperationOutcome.Result<Object, Class<?>> interfaceTypeReflectiveOperationOutcomeResult))
			// Semantically, this can only be CLASS_NOT_FOUND.
			return interfaceTypeReflectiveOperationOutcome;

		// This cast is guaranteed to work by semantics of nameToClass.
		Class<?> interfaceType = (Class<?>) interfaceTypeReflectiveOperationOutcomeResult.returnValue();

		return createProxyInstance(interfaceType, (proxy, method, args) ->
		{
			CallbackOperationOutcome<Object, Object> result = callbacks.callCallbackInstanceMethod(
					interfaceType, method.getName(), method.getReturnType(), List.of(method.getParameterTypes()),
					proxy, argsToList(args));

			return switch(result.kind())
			{
				case CALLBACK_RESULT -> ((CallbackOperationOutcome.Result<Object, Object>) result).returnValue();
				case CALLBACK_THROWN -> throw (Throwable) ((CallbackOperationOutcome.Thrown<Object, Object>) result).thrownThrowable();
				case CALLBACK_HIDDEN_ERROR -> throw new HiddenCallbackErrorException();
			};
		});
	}

	@Override
	public InternalCallbackManager<Object> getCallbackManager()
	{
		return this;
	}

	public static <TC extends Transceiver> UninitializedStudentSideCommunicator<Object, Class<?>, TC, InternalCallbackManager<Object>>
			createUninitializedCommunicator(Function<StudentSideCommunicatorCallbacks<Object, Class<?>>, TC> createTransceiver)
	{
		return callbacks -> new DirectSameJVMCommunicator<>(callbacks, createTransceiver);
	}
}
