package net.haspamelodica.charon.communicator.impl.samejvm;

import static net.haspamelodica.charon.reflection.ReflectionUtils.argsToList;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;
import static net.haspamelodica.charon.reflection.ReflectionUtils.createProxyInstance;
import static net.haspamelodica.charon.reflection.ReflectionUtils.nameToClass;

import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class DirectSameJVMCommunicator<TC extends Transceiver>
		implements StudentSideCommunicator<Object, TC, InternalCallbackManager<Object>>, InternalCallbackManager<Object>
{
	private final StudentSideCommunicatorCallbacks<Object> callbacks;

	private final TC transceiver;

	public DirectSameJVMCommunicator(StudentSideCommunicatorCallbacks<Object> callbacks,
			Function<StudentSideCommunicatorCallbacks<Object>, TC> createTransceiver)
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
	public String getClassname(Object ref)
	{
		return classToName(ref.getClass());
	}

	@Override
	public String getSuperclass(String cn)
	{
		return classToName(nameToClass(cn).getSuperclass());
	}

	@Override
	public List<String> getInterfaces(String cn)
	{
		return classToName(List.of(nameToClass(cn).getInterfaces()));
	}

	@Override
	public RefOrError<Object> callConstructor(String cn, List<String> params, List<Object> argRefs)
	{
		return handleTargetExceptions(() -> ReflectionUtils.callConstructor(nameToClass(cn), nameToClass(params), argRefs));
	}

	@Override
	public RefOrError<Object> callStaticMethod(String cn, String name, String returnClassname, List<String> params,
			List<Object> argRefs)
	{
		return handleTargetExceptions(() -> ReflectionUtils
				.callStaticMethod(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params), argRefs));
	}

	@Override
	public Object getStaticField(String cn, String name, String fieldClassname)
	{
		return ReflectionUtils.getStaticField(nameToClass(cn), name, nameToClass(fieldClassname));
	}

	@Override
	public void setStaticField(String cn, String name, String fieldClassname, Object valueRef)
	{
		setStaticField_(nameToClass(cn), name, nameToClass(fieldClassname), valueRef);
	}
	// extracted to method so the cast is expressible in Java
	private <F> void setStaticField_(Class<?> clazz, String name, Class<F> fieldClass, Object value)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		F valueCasted = (F) value;
		ReflectionUtils.setStaticField(clazz, name, fieldClass, valueCasted);
	}

	@Override
	public RefOrError<Object> callInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			Object receiverRef, List<Object> argRefs)
	{
		return callInstanceMethod_(nameToClass(cn), name, nameToClass(returnClassname), nameToClass(params),
				receiverRef, argRefs);
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
	public Object getInstanceField(String cn, String name, String fieldClassname, Object receiverRef)
	{
		return getInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), receiverRef);
	}
	// extracted to method so the cast is expressible in Java
	private <T> Object getInstanceField_(Class<T> clazz, String name, Class<?> fieldClass, Object receiver)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T receiverCasted = (T) receiver;
		return ReflectionUtils.getInstanceField(clazz, name, fieldClass, receiverCasted);
	}

	@Override
	public void setInstanceField(String cn, String name, String fieldClassname,
			Object receiverRef, Object valueRef)
	{
		setInstanceField_(nameToClass(cn), name, nameToClass(fieldClassname), receiverRef, valueRef);
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
		return createProxyInstance(nameToClass(interfaceCn), (proxy, method, args) ->
		{
			RefOrError<Object> result = callbacks.callCallbackInstanceMethod(
					interfaceCn, method.getName(), classToName(method.getReturnType()), classToName(method.getParameterTypes()),
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

	public static <TC extends Transceiver> UninitializedStudentSideCommunicator<Object, TC, InternalCallbackManager<Object>>
			createUninitializedCommunicator(Function<StudentSideCommunicatorCallbacks<Object>, TC> createTransceiver)
	{
		return callbacks -> new DirectSameJVMCommunicator<>(callbacks, createTransceiver);
	}
}
