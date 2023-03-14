package net.haspamelodica.charon.communicator.impl;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCommunicatorSupplier;

public class LoggingCommunicator<REF, COMM extends StudentSideCommunicator<REF>> implements StudentSideCommunicator<REF>
{
	public static final String DEFAULT_PREFIX = "";

	protected final COMM communicator;

	private final String prefix;

	public LoggingCommunicator(COMM communicator, String prefix)
	{
		this.communicator = communicator;
		this.prefix = prefix;
	}
	public LoggingCommunicator(UninitializedStudentSideCommunicator<REF, COMM> communicator, StudentSideCommunicatorCallbacks<REF> callbacks, String prefix)
	{
		this.communicator = communicator.initialize(new LoggingStudentSideCommunicatorCallbacks(callbacks));
		this.prefix = prefix;
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
		log("classname " + ref);
		return communicator.getClassname(ref);
	}
	@Override
	public String getSuperclass(String cn)
	{
		log("superclass " + cn);
		return communicator.getSuperclass(cn);
	}
	@Override
	public List<String> getInterfaces(String cn)
	{
		log("interfaces " + cn);
		return communicator.getInterfaces(cn);
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		log("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callConstructor(cn, params, argRefs);
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs.toString());
		return communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
	}
	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		log(fieldClassname + " " + cn + "." + name);
		return communicator.getStaticField(cn, name, fieldClassname);
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
	}

	@Override
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
	{
		log(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs.toString());
		return communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
	}
	@Override
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		return communicator.getInstanceField(cn, name, fieldClassname, receiverRef);
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef)
	{
		log(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
	}

	@Override
	public REF createCallbackInstance(String interfaceCn)
	{
		log("new callback " + interfaceCn);
		return communicator.createCallbackInstance(interfaceCn);
	}

	protected void log(String message)
	{
		System.err.println(prefix + message);
	}

	protected class LoggingStudentSideCommunicatorCallbacks implements StudentSideCommunicatorCallbacks<REF>
	{
		private final StudentSideCommunicatorCallbacks<REF> callbacks;

		public LoggingStudentSideCommunicatorCallbacks(StudentSideCommunicatorCallbacks<REF> callbacks)
		{
			this.callbacks = callbacks;
		}

		@Override
		public String getCallbackInterfaceCn(REF ref)
		{
			log("callback interface " + ref);
			return callbacks.getCallbackInterfaceCn(ref);
		}

		@Override
		public REF callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
		{
			log("callback " + returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs.toString());
			return callbacks.callCallbackInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
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
			return new LoggingCommunicator<>(communicatorSupplier.createCommunicator(storeRefsIdentityBased, callbacks), prefix);
		}
	}
}
