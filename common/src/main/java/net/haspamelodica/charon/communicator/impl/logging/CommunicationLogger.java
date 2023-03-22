package net.haspamelodica.charon.communicator.impl.logging;

import java.util.concurrent.atomic.AtomicInteger;

public class CommunicationLogger
{
	public static final String DEFAULT_PREFIX = "";

	private final String prefix;

	private final ThreadLocal<Integer>	threadId;
	private final ThreadLocal<Integer>	nestingDepth;

	public CommunicationLogger()
	{
		this(DEFAULT_PREFIX);
	}
	public CommunicationLogger(String prefix)
	{
		this.prefix = prefix;
		this.threadId = ThreadLocal.withInitial(new AtomicInteger()::incrementAndGet);
		this.nestingDepth = ThreadLocal.withInitial(() -> 0);
	}

	public void logEnter(String message)
	{
		log(false, false, message);
	}
	public void logExit()
	{
		log(false, true, null);
	}
	public void logExit(Object result)
	{
		log(false, true, String.valueOf(result));
	}
	public void logEnterCallback(String message)
	{
		log(true, false, message);
	}
	public void logExitCallback()
	{
		log(true, true, null);
	}
	public void logExitCallback(Object result)
	{
		log(true, true, String.valueOf(result));
	}
	public void log(boolean callback, boolean exit, String message)
	{
		int nestingDepth = this.nestingDepth.get();
		if(!exit)
			nestingDepth ++;

		System.err.println(prefix + "T" + threadId.get() + "\t".repeat(nestingDepth) + (callback ? exit ? "=>" : "<-" : exit ? "<=" : "->") +
				(!exit || message != null ? " " + message : ""));

		if(exit)
			nestingDepth --;
		this.nestingDepth.set(nestingDepth);
	}
}