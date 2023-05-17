package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClass;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription.Kind;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.marshaling.SerDes;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;
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
	public Class<?> getTypeByName(String typeName)
	{
		return nameToClass(typeName);
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
	public Object newArray(Class<?> componentType, int length)
	{
		return ReflectionUtils.newArray(componentType, length);
	}

	@Override
	public Object newMultiArray(Class<?> componentType, List<Integer> dimensions)
	{
		return ReflectionUtils.newMultiArray(componentType, dimensions);
	}

	@Override
	public Object newArrayWithInitialValues(Class<?> componentType, List<Object> initialValues)
	{
		return ReflectionUtils.newArrayWithInitialValues(componentType, initialValues);
	}

	@Override
	public int getArrayLength(Object arrayRef)
	{
		return ReflectionUtils.getArrayLength(arrayRef);
	}

	@Override
	public Object getArrayElement(Object arrayRef, int index)
	{
		return ReflectionUtils.getArrayElement(arrayRef, index);
	}

	@Override
	public void setArrayElement(Object arrayRef, int index, Object valueRef)
	{
		ReflectionUtils.setArrayElement(arrayRef, index, valueRef);
	}

	@Override
	public RefOrError<Object> callConstructor(Class<?> type, List<Class<?>> params, List<Object> argRefs)
	{
		return handleTargetExceptions(() -> ReflectionUtils.callConstructor(type, params, argRefs));
	}

	@Override
	public RefOrError<Object> callStaticMethod(Class<?> type, String name, Class<?> returnType, List<Class<?>> params,
			List<Object> argRefs)
	{
		return handleTargetExceptions(() -> ReflectionUtils.callStaticMethod(type, name, returnType, params, argRefs));
	}

	@Override
	public Object getStaticField(Class<?> type, String name, Class<?> fieldType)
	{
		return ReflectionUtils.getStaticField(type, name, fieldType);
	}

	@Override
	public void setStaticField(Class<?> type, String name, Class<?> fieldType, Object valueRef)
	{
		setStaticField_(type, name, fieldType, valueRef);
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public RefOrError<Object> callInstanceMethod(Class<?> type, String name, Class<?> returnType, List<Class<?>> params,
			Object receiverRef, List<Object> argRefs)
	{
		return callInstanceMethod_(type, name, returnType, params, receiverRef, argRefs);
	}
	// extracted to method so the cast is expressible in Java
	private <T> RefOrError<Object> callInstanceMethod_(Class<T> clazz, String name, Class<?> returnClass, List<Class<?>> params,
			Object receiver, List<Object> args)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return handleTargetExceptions(() -> ReflectionUtils.callInstanceMethod(clazz, name, returnClass, params, receiverCasted, args));
	}

	@Override
	public Object getInstanceField(Class<?> type, String name, Class<?> fieldType, Object receiverRef)
	{
		return getInstanceField_(type, name, fieldType, receiverRef);
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object getInstanceField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getInstanceField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public void setInstanceField(Class<?> type, String name, Class<?> fieldType, Object receiverRef, Object valueRef)
	{
		setInstanceField_(type, name, fieldType, receiverRef, valueRef);
	}
	// extracted to method so the casts are expressible in Java
	private <T, F> void setInstanceField_(Class<T> clazz, String name, Class<F> fieldClass, Object receiver, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setInstanceField(clazz, name, fieldClass, receiverCasted, valueCasted);
	}

	@Override
	public TC getTransceiver()
	{
		return transceiver;
	}

	@Override
	public Object createCallbackInstance(String interfaceCn)
	{
		Class<?> interfaceType = nameToClass(interfaceCn);
		return createProxyInstance(interfaceType, (proxy, method, args) ->
		{
			RefOrError<Object> result = callbacks.callCallbackInstanceMethod(
					interfaceType, method.getName(), method.getReturnType(), List.of(method.getParameterTypes()),
					proxy, argsToList(args));
			if(result.isError())
				throw (Throwable) result.resultOrErrorRef();
			return result.resultOrErrorRef();
		});
	}

	private RefOrError<Object> handleTargetExceptions(ReflectiveSupplier<Object> action)
	{
		try
		{
			return RefOrError.success(action.get());
		} catch(ExceptionInTargetException e)
		{
			return RefOrError.error(e.getTargetThrowable());
		}
	}
	@FunctionalInterface
	private static interface ReflectiveSupplier<R>
	{
		public R get() throws ExceptionInTargetException;
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
