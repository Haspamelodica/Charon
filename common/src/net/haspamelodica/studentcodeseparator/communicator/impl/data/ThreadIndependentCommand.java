package net.haspamelodica.studentcodeseparator.communicator.impl.data;

public enum ThreadIndependentCommand
{
	//thread-independent commands
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
