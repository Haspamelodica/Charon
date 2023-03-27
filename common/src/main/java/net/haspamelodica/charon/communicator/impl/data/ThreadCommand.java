package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadCommand
{
	EXERCISE_FINISHED,
	EXERCISE_ERROR,

	//no callback possible
	GET_STATIC_FIELD,
	SET_STATIC_FIELD,
	GET_INSTANCE_FIELD,
	SET_INSTANCE_FIELD,
	CREATE_CALLBACK_INSTANCE,
	GET_CLASSNAME,
	GET_SUPERCLASS,
	GET_INTERFACES,

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
