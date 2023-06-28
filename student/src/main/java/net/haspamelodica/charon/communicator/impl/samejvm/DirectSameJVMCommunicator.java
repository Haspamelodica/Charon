package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClassWrapReflectiveAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

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
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

// TODO maybe switch to newer, faster Handles (MethodHandle, VarHandle etc.)
public class DirectSameJVMCommunicator<TC extends Transceiver>
		implements StudentSideCommunicator<Object, Throwable, Class<?>, Constructor<?>, Method, Field,
				TC, InternalCallbackManager<Object>>,
		InternalCallbackManager<Object>
{
	private final StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks;

	private final UnidirectionalMap<Object, Object> primitivesCache;

	private final TC transceiver;

	public DirectSameJVMCommunicator(StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks,
			Function<StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>>, TC> createTransceiver)
	{
		this.callbacks = callbacks;
		this.primitivesCache = UnidirectionalMap.builder().concurrent().build();
		this.transceiver = createTransceiver.apply(this.callbacks);
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return true;
	}

	@Override
	public OperationOutcome<Class<?>, Void, Class<?>> getTypeByName(String typeName)
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
	public OperationOutcome<Object, Void, Class<?>> createArray(Class<?> arrayType, int length)
	{
		return ReflectionUtils.createArray(arrayType, length);
	}

	@Override
	public OperationOutcome<Object, Void, Class<?>> createMultiArray(Class<?> arrayType, List<Integer> dimensions)
	{
		return ReflectionUtils.createMultiArray(arrayType, dimensions);
	}

	@Override
	public OperationOutcome<Object, Void, Class<?>> initializeArray(Class<?> arrayType, List<Object> initialValues)
	{
		return ReflectionUtils.initializeArray(arrayType, initialValues);
	}

	@Override
	public int getArrayLength(Object arrayRef)
	{
		return ReflectionUtils.getArrayLength(arrayRef);
	}

	@Override
	public OperationOutcome<Object, Void, Class<?>> getArrayElement(Object arrayRef, int index)
	{
		return lookupCachedPrimitiveIfPrimitive(getTypeOf(arrayRef).componentType(), ReflectionUtils.getArrayElement(arrayRef, index));
	}

	@Override
	public OperationOutcome<Void, Void, Class<?>> setArrayElement(Object arrayRef, int index, Object valueRef)
	{
		return ReflectionUtils.setArrayElement(arrayRef, index, valueRef);
	}

	@Override
	public OperationOutcome<Constructor<?>, Void, Class<?>> lookupConstructor(Class<?> type, List<Class<?>> params)
	{
		return ReflectionUtils.lookupConstructor(type, params);
	}

	@Override
	public OperationOutcome<Method, Void, Class<?>> lookupMethod(
			Class<?> type, String name, Class<?> returnType, List<Class<?>> params, boolean isStatic)
	{
		return ReflectionUtils.lookupMethod(type, name, returnType, params, isStatic);
	}

	@Override
	public OperationOutcome<Field, Void, Class<?>> lookupField(Class<?> type, String name, Class<?> fieldType, boolean isStatic)
	{
		return ReflectionUtils.lookupField(type, name, fieldType, isStatic);
	}

	@Override
	public OperationOutcome<Object, Throwable, Class<?>> callConstructor(Constructor<?> constructor, List<Object> argRefs)
	{
		return ReflectionUtils.callConstructor(constructor, argRefs);
	}

	@Override
	public OperationOutcome<Object, Throwable, Class<?>> callStaticMethod(Method method, List<Object> argRefs)
	{
		return lookupCachedPrimitiveIfPrimitive(method.getReturnType(), ReflectionUtils.callStaticMethod(method, argRefs));
	}

	@Override
	public OperationOutcome<Object, Void, Class<?>> getStaticField(Field field)
	{
		return lookupCachedPrimitiveIfPrimitive(field.getType(), ReflectionUtils.getStaticField(field));
	}

	@Override
	public OperationOutcome<Void, Void, Class<?>> setStaticField(Field field, Object valueRef)
	{
		return ReflectionUtils.setStaticField(field, valueRef);
	}

	@Override
	public OperationOutcome<Object, Throwable, Class<?>> callInstanceMethod(Method method, Object receiverRef, List<Object> argRefs)
	{
		return ReflectionUtils.callInstanceMethod(method, receiverRef, argRefs);
	}

	@Override
	public OperationOutcome<Object, Void, Class<?>> getInstanceField(Field field, Object receiverRef)
	{
		return ReflectionUtils.getInstanceField(field, receiverRef);
	}

	@Override
	public OperationOutcome<Void, Void, Class<?>> setInstanceField(Field field, Object receiverRef, Object valueRef)
	{
		return ReflectionUtils.setInstanceField(field, receiverRef, valueRef);
	}

	@Override
	public TC getTransceiver()
	{
		return transceiver;
	}

	@Override
	public Object createCallbackInstance(String interfaceCn)
	{
		OperationOutcome<Class<?>, Void, Class<?>> interfaceTypeReflectiveOperationOutcome = nameToClassWrapReflectiveAction(interfaceCn);
		if(!(interfaceTypeReflectiveOperationOutcome instanceof OperationOutcome.Result<
				Class<?>, Void, Class<?>> interfaceTypeReflectiveOperationOutcomeResult))
			// Semantically, this can only be CLASS_NOT_FOUND.
			return interfaceTypeReflectiveOperationOutcome;

		Class<?> interfaceType = interfaceTypeReflectiveOperationOutcomeResult.returnValue();

		return createProxyInstance(interfaceType, (proxy, method, args) ->
		{
			List<Class<?>> params = List.of(method.getParameterTypes());
			CallbackOperationOutcome<Object, Throwable> result = callbacks.callCallbackInstanceMethod(
					interfaceType, method.getName(), method.getReturnType(), params,
					proxy, argsToListAndLookupCachedPrimitives(params, args));

			return switch(result.kind())
			{
				case CALLBACK_RESULT -> ((CallbackOperationOutcome.Result<Object, Throwable>) result).returnValue();
				case CALLBACK_THROWN -> throw ((CallbackOperationOutcome.Thrown<Object, Throwable>) result).thrownThrowable();
				case CALLBACK_HIDDEN_ERROR -> throw new HiddenCallbackErrorException();
			};
		});
	}

	@Override
	public InternalCallbackManager<Object> getCallbackManager()
	{
		return this;
	}

	private List<Object> argsToListAndLookupCachedPrimitives(List<Class<?>> params, Object[] args)
	{
		return IntStream
				.range(0, args.length)
				.mapToObj(i -> lookupCachedPrimitiveIfPrimitive(params.get(i), args[i]))
				.toList();
	}
	private <THROWABLEREF> OperationOutcome<Object, THROWABLEREF, Class<?>> lookupCachedPrimitiveIfPrimitive(Class<?> type,
			OperationOutcome<Object, THROWABLEREF, Class<?>> outcome)
	{
		if(!(outcome instanceof OperationOutcome.Result<Object, THROWABLEREF, Class<?>> result))
			return outcome;

		// void counts as primitive for some reason and would cause NPEs later on (specifically, when looking up primitive cache)
		if(!type.isPrimitive() || type == void.class)
			return outcome;

		return new OperationOutcome.Result<>(lookupCachedPrimitive(result.returnValue()));
	}
	private Object lookupCachedPrimitiveIfPrimitive(Class<?> type, Object object)
	{
		// void counts as primitive for some reason and would cause NPEs later on (specifically, when looking up primitive cache)
		if(!type.isPrimitive() || type == void.class)
			return object;
		else
			return lookupCachedPrimitive(object);
	}
	private Object lookupCachedPrimitive(Object primitive)
	{
		// Don't use primitive.getClass() to determine whether the object is a primitive or not,
		// because that would break object identity when the student / exercise side actually transfers a primitive box object
		return primitivesCache.computeIfAbsent(primitive, Function.identity());
	}

	public static <TC extends Transceiver> UninitializedStudentSideCommunicator<Object, Throwable, Class<?>, Constructor<?>, Method, Field,
			TC, InternalCallbackManager<Object>>
			createUninitializedCommunicator(Function<StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>>, TC> createTransceiver)
	{
		return callbacks -> new DirectSameJVMCommunicator<>(callbacks, createTransceiver);
	}
}
