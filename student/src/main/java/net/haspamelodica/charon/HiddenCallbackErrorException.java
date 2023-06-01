package net.haspamelodica.charon;

public class HiddenCallbackErrorException extends RuntimeException
{
	public HiddenCallbackErrorException()
	{
		super("Something went wrong in the exercise side");
	}
}
