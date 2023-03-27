package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadResponse
{
	STUDENT_FINISHED,
	STUDENT_ERROR,

	GET_CALLBACK_INTERFACE_CN,
	CALL_CALLBACK_INSTANCE_METHOD,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadResponse decode(byte raw)
	{
		return raw < 0 || raw >= values().length ? null : values()[raw];
	}
}
