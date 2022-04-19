package net.haspamelodica.studentcodeseparator.utils;

public class Args
{
	private final String	usage;
	private final String[]	args;
	private int				nextArgIndex;

	public Args(String usage, String[] args)
	{
		this.usage = usage;
		this.args = args;
		this.nextArgIndex = 0;
	}

	public int remaining()
	{
		return args.length - nextArgIndex;
	}
	public boolean hasNext()
	{
		return remaining() > 0;
	}
	public String consume()
	{
		checkHasNext();
		return args[nextArgIndex ++];
	}
	public int consumeInteger()
	{
		String string = consume();
		try
		{
			return Integer.parseInt(string);
		} catch(NumberFormatException e)
		{
			return throwUsage("Expected a number: " + string);
		}
	}

	public boolean consumeIfEqual(String expected)
	{
		if(!hasNext())
			return false;
		boolean match = args[nextArgIndex].equals(expected);
		if(match)
			nextArgIndex ++;
		return match;
	}
	public String peek()
	{
		checkHasNext();
		return args[nextArgIndex];
	}
	public void expectEnd()
	{
		if(hasNext())
			throwUsage();
	}

	private void checkHasNext()
	{
		if(!hasNext())
			throwUsage();
	}

	public <R> R throwUsage()
	{
		return throwUsage(null);
	}
	public <R> R throwUsage(String detail)
	{
		throw new IllegalArgumentException((detail != null ? detail + "\n" : "") + usage);
	}
}