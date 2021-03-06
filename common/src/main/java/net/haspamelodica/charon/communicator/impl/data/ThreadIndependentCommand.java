package net.haspamelodica.charon.communicator.impl.data;

public enum ThreadIndependentCommand
{
	NEW_THREAD,
	REF_DELETED,
	SHUTDOWN,
	;

	public byte encode()
	{
		return (byte) ordinal();
	}
	public static ThreadIndependentCommand decode(byte raw)
	{
		return values()[raw];
	}
}
