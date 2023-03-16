package net.haspamelodica.charon.communicator.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;

public class LoggingCommunicator<REF, COMM extends StudentSideCommunicator<REF>> implements StudentSideCommunicator<REF>
{
	public static final String DEFAULT_PREFIX = "";

	protected final COMM communicator;

	private final String prefix;

	private final ThreadLocal<Integer>	threadId;
	private final ThreadLocal<Integer>	nestingDepth;

	public LoggingCommunicator(COMM communicator, String prefix)
	{
		this(c -> communicator, prefix);
	}
	public LoggingCommunicator(UninitializedStudentSideCommunicator<REF, COMM> communicator, StudentSideCommunicatorCallbacks<REF> callbacks, String prefix)
	{
		this(c -> communicator.initialize(c.new LoggingStudentSideCommunicatorCallbacks<>(callbacks)), prefix);
	}
	protected LoggingCommunicator(Function<LoggingCommunicator<REF, COMM>, COMM> createCommunicator, String prefix)
	{
		this.communicator = createCommunicator.apply(this);
		this.prefix = prefix;
		this.threadId = ThreadLocal.withInitial(new AtomicInteger()::incrementAndGet);
		this.nestingDepth = ThreadLocal.withInitial(() -> 0);
	}

	public static <REF> UninitializedStudentSideCommunicator<REF, ?>
			maybeWrapLogging(UninitializedStudentSideCommunicator<REF, ?> communicator, boolean logging)
	{
		return maybeWrapLogging(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> UninitializedStudentSideCommunicator<REF, ?>
			maybeWrapLogging(UninitializedStudentSideCommunicator<REF, ?> communicator, String prefix, boolean logging)
	{
		if(logging)
			return (UninitializedStudentSideCommunicator<REF, StudentSideCommunicator<REF>>) c -> new LoggingCommunicator<>(communicator, c, prefix);
		return communicator;
	}
	public static <REF> StudentSideCommunicator<REF> maybeWrapLogging(StudentSideCommunicator<REF> communicator, boolean logging)
	{
		return maybeWrapLogging(communicator, DEFAULT_PREFIX, logging);
	}
	public static <REF> StudentSideCommunicator<REF> maybeWrapLogging(StudentSideCommunicator<REF> communicator, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingCommunicator<>(communicator, prefix);
		return communicator;
	}
	public static RefTranslatorCommunicatorSupplier maybeWrapLogging(RefTranslatorCommunicatorSupplier communicatorSupplier, boolean logging)
	{
		return maybeWrapLogging(communicatorSupplier, DEFAULT_PREFIX, logging);
	}
	public static RefTranslatorCommunicatorSupplier maybeWrapLogging(RefTranslatorCommunicatorSupplier communicatorSupplier, String prefix, boolean logging)
	{
		if(logging)
			return new LoggingRefTranslatorCommunicatorSupplier<>(communicatorSupplier, prefix);
		return communicatorSupplier;
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return communicator.storeRefsIdentityBased();
	}

	@Override
	public String getClassname(REF ref)
	{
		logEnter("classname " + ref);
		String result = communicator.getClassname(ref);
		logExit(result);
		return result;
	}
	@Override
	public String getSuperclass(String cn)
	{
		logEnter("superclass " + cn);
		String result = communicator.getSuperclass(cn);
		logExit(result);
		return result;
	}
	@Override
	public List<String> getInterfaces(String cn)
	{
		logEnter("interfaces " + cn);
		List<String> result = communicator.getInterfaces(cn);
		logExit(result);
		return result;
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		logEnter("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		REF result = communicator.callConstructor(cn, params, argRefs);
		logExit(result);
		return result;
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs)
	{
		logEnter(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		REF result = communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
		logExit(result);
		return result;
	}
	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		logEnter(fieldClassname + " " + cn + "." + name);
		REF result = communicator.getStaticField(cn, name, fieldClassname);
		logExit(result);
		return result;
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
	{
		logEnter(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
		logExit();
	}

	@Override
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
	{
		logEnter(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
		REF result = communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
		logExit(result);
		return result;
	}
	@Override
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef)
	{
		logEnter(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		REF result = communicator.getInstanceField(cn, name, fieldClassname, receiverRef);
		logExit(result);
		return result;
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef)
	{
		logEnter(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
		logExit();
	}

	@Override
	public REF createCallbackInstance(String interfaceCn)
	{
		logEnter("new callback " + interfaceCn);
		REF result = communicator.createCallbackInstance(interfaceCn);
		logExit(result);
		return result;
	}

	protected void logEnter(String message)
	{
		log(false, false, message);
	}
	protected void logExit()
	{
		log(false, true, null);
	}
	protected void logExit(Object result)
	{
		log(false, true, String.valueOf(result));
	}
	protected void logEnterCallback(String message)
	{
		log(true, false, message);
	}
	protected void logExitCallback()
	{
		log(true, true, null);
	}
	protected void logExitCallback(Object result)
	{
		log(true, true, String.valueOf(result));
	}
	protected void log(boolean callback, boolean exit, String message)
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

	protected class LoggingStudentSideCommunicatorCallbacks<CB extends StudentSideCommunicatorCallbacks<REF>> implements StudentSideCommunicatorCallbacks<REF>
	{
		protected final CB callbacks;

		public LoggingStudentSideCommunicatorCallbacks(CB callbacks)
		{
			this.callbacks = callbacks;
		}

		@Override
		public String getCallbackInterfaceCn(REF ref)
		{
			logEnterCallback("callback interface " + ref);
			String result = callbacks.getCallbackInterfaceCn(ref);
			logExitCallback(result);
			return result;
		}

		@Override
		public REF callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
		{
			logEnterCallback("callback " + returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
			REF result = callbacks.callCallbackInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
			logExitCallback(result);
			return result;
		}
	}

	protected class LoggingRefTranslatorCommunicatorCallbacks extends LoggingStudentSideCommunicatorCallbacks<RefTranslatorCommunicatorCallbacks<REF>>
			implements RefTranslatorCommunicatorCallbacks<REF>
	{
		public LoggingRefTranslatorCommunicatorCallbacks(RefTranslatorCommunicatorCallbacks<REF> callbacks)
		{
			super(callbacks);
		}

		@Override
		public <REF_FROM> REF createForwardRef(UntranslatedRef<REF_FROM> untranslatedRef)
		{
			return callbacks.createForwardRef(untranslatedRef);
		}
	}

	protected static class LoggingRefTranslatorCommunicatorSupplier<SUPP extends RefTranslatorCommunicatorSupplier>
			implements RefTranslatorCommunicatorSupplier
	{
		protected final SUPP	communicatorSupplier;
		protected final String	prefix;

		protected LoggingRefTranslatorCommunicatorSupplier(SUPP communicatorSupplier, String prefix)
		{
			this.communicatorSupplier = communicatorSupplier;
			this.prefix = prefix;
		}

		@Override
		public <REF_TO> StudentSideCommunicator<REF_TO> createCommunicator(
				boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
		{
			return new LoggingCommunicator<>(comm -> communicatorSupplier.createCommunicator(
					storeRefsIdentityBased, comm.new LoggingRefTranslatorCommunicatorCallbacks(callbacks)), prefix);
		}
	}
}
