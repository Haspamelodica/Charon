package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadIndependentRequest
{
	SHUTDOWN_FINISHED,
	CRASHED,
	OUT_OF_MEMORY,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadIndependentRequest decode(byte raw)
	{
		return values()[raw];
	}
}
