package net.haspamelodica.charon.communicator.impl.logging;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CommunicationLogger<TYPEREF>
{
	public static final String DEFAULT_PREFIX = "";

	private final CommunicationLoggerParams	params;
	private final Function<TYPEREF, String>	typerefToTypeName;

	private final ThreadLocal<Integer>	threadId;
	private final ThreadLocal<Integer>	nestingDepth;

	public CommunicationLogger(CommunicationLoggerParams params, Function<TYPEREF, String> typerefToTypeName)
	{
		this.params = params;
		this.typerefToTypeName = typerefToTypeName;
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

		System.err.println(params.prefix() + "T" + threadId.get() + "\t".repeat(nestingDepth) + (callback ? exit ? "=>" : "<-" : exit ? "<=" : "->") +
				(!exit || message != null ? " " + message : ""));

		if(exit)
			nestingDepth --;
		this.nestingDepth.set(nestingDepth);
	}

	public String typerefToString(TYPEREF typeref)
	{
		return typeref == null ? "<null type>" : "<T" + typeref + " " + typerefToTypeName(typeref) + ">";
	}

	private String typerefToTypeName(TYPEREF typeref)
	{
		return typerefToTypeName.apply(typeref);
	}
}