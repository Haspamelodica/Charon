package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadIndependentResponse
{
	SHUTDOWN_FINISHED,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadIndependentResponse decode(byte raw)
	{
		return values()[raw];
	}
}
