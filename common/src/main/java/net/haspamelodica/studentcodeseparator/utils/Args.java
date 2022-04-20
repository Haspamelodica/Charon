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
	public void expect(String expected)
	{
		String actual = consume();
		if(!actual.equals(expected))
			throwUsage("Expected " + expected + ", but was " + actual);
	}
	public String peek()
	{
		checkHasNext();
		return args[nextArgIndex];
	}

	public int remaining()
	{
		return args.length - nextArgIndex;
	}
	public boolean hasNext()
	{
		return remaining() > 0;
	}
	public void expectEnd()
	{
		if(hasNext())
			throwUsage("Too many arguments given");
	}

	private void checkHasNext()
	{
		if(!hasNext())
			throwUsage("Too few arguments given");
	}

	public <R> R throwUsage(String detail)
	{
		throw new IllegalArgumentException(detail + "\n" + usage);
	}
}