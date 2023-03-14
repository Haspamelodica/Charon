package net.haspamelodica.charon.marshaling;

import java.util.List;

public interface MarshalingCommunicatorCallbacks<M> extends RepresentationObjectMarshaler
{
	public CallbackMethod<M> lookupCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params, Object receiver);
	/** This method will only be called for methods where lookupCallbackInstanceMethod has been called before. */
	public Object callCallbackInstanceMethodChecked(CallbackMethod<M> callbackMethod, Object receiver, List<Object> args);

	public static record CallbackMethod<M>(Class<?> receiverType, Class<?> returnType, List<Class<?>> paramTypes, M methodData)
	{}
}
