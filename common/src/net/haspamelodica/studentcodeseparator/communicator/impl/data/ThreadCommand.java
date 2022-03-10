package net.haspamelodica.studentcodeseparator.communicator.impl.data;

public enum ThreadCommand
{
	//no callback possible
	GET_STATIC_FIELD,
	SET_STATIC_FIELD,
	GET_INSTANCE_FIELD,
	SET_INSTANCE_FIELD,

	//callback possible
	GET_CLASSNAME,
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
