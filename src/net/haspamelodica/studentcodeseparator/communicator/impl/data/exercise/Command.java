package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

public enum Command
{
	GET_CLASSNAME,
	SEND,
	RECEIVE,
	CALL_CONSTRUCTOR,
	CALL_STATIC_METHOD,
	GET_STATIC_FIELD,
	SET_STATIC_FIELD,
	CALL_INSTANCE_METHOD,
	GET_INSTANCE_FIELD,
	SET_INSTANCE_FIELD;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static Command decode(byte raw)
	{
		return values()[raw];
	}
}
