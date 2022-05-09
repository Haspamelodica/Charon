package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadResponse
{
	RETURNED,
	CALLBACK,
	SERIALIZER_READY,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadResponse decode(byte raw)
	{
		return values()[raw];
	}
}
