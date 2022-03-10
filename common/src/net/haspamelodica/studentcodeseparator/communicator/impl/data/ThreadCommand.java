package net.haspamelodica.studentcodeseparator.communicator.impl.data;

public enum ThreadCommand
{
	//thread commands with no callback possibility
	GET_STATIC_FIELD,
	SET_STATIC_FIELD,
	GET_INSTANCE_FIELD,
	SET_INSTANCE_FIELD,

	//thread commands with callback possibility
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
