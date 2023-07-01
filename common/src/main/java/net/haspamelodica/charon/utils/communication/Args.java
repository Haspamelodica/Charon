package net.haspamelodica.charon.utils.communication;

public class Args
{
	private final String[]	args;
	private int				nextArgIndex;

	public Args(String[] args)
	{
		this.args = args;
		this.nextArgIndex = 0;
	}

	public String consume() throws IncorrectUsageException
	{
		checkHasNext();
		return args[nextArgIndex ++];
	}
	public int consumeInteger() throws IncorrectUsageException
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
	public void expect(String expected) throws IncorrectUsageException
	{
		String actual = consume();
		if(!actual.equals(expected))
			throwUsage("Expected " + expected + ", but was " + actual);
	}
	public String peek() throws IncorrectUsageException
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
	public void expectEnd() throws IncorrectUsageException
	{
		if(hasNext())
			throwUsage("Too many arguments given");
	}

	private void checkHasNext() throws IncorrectUsageException
	{
		if(!hasNext())
			throwUsage("Too few arguments given");
	}

	public <R> R throwUsage(String detail) throws IncorrectUsageException
	{
		throw new IncorrectUsageException(detail);
	}
}
