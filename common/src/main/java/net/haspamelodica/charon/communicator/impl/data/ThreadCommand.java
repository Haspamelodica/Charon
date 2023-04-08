package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadCommand
{
	EXERCISE_FINISHED,
	EXERCISE_ERROR,

	//no callback possible
	//TODO theoretically classloading could trigger a callback.
	GET_TYPE_BY_NAME,
	GET_ARRAY_TYPE,
	GET_TYPE_OF,
	DESCRIBE_TYPE,
	NEW_ARRAY,
	NEW_MULTI_ARRAY,
	GET_ARRAY_LENGTH,
	GET_ARRAY_ELEMENT,
	SET_ARRAY_ELEMENT,
	GET_STATIC_FIELD,
	SET_STATIC_FIELD,
	GET_INSTANCE_FIELD,
	SET_INSTANCE_FIELD,
	CREATE_CALLBACK_INSTANCE,

	//callback possible
	SEND,
	RECEIVE,
	CALL_CONSTRUCTOR,
	CALL_STATIC_METHOD,
	CALL_INSTANCE_METHOD,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadCommand decode(byte raw)
	{
		return values()[raw];
	}
}
