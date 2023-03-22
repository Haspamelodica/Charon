package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.Transceiver;

public class LoggingCommunicator<REF, TC extends Transceiver, CM extends CallbackManager> implements StudentSideCommunicator<REF, TC, CM>
{
	private final CommunicationLogger logger;

	private final StudentSideCommunicator<REF, ? extends TC, ? extends CM> communicator;

	private final TC	loggingTransceiver;
	private final CM	loggingCallbackManager;

	public LoggingCommunicator(CommunicationLogger logger,
			StudentSideCommunicator<REF, ? extends TC, ? extends CM> communicator, TC loggingTransceiver, CM loggingCallbackManager)
	{
		this.logger = logger;
		this.communicator = communicator;
		this.loggingTransceiver = loggingTransceiver;
		this.loggingCallbackManager = loggingCallbackManager;
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return communicator.storeRefsIdentityBased();
	}

	@Override
	public String getClassname(REF ref)
	{
		logger.logEnter("classname " + ref);
		String result = communicator.getClassname(ref);
		logger.logExit(result);
		return result;
	}
	@Override
	public String getSuperclass(String cn)
	{
		logger.logEnter("superclass " + cn);
		String result = communicator.getSuperclass(cn);
		logger.logExit(result);
		return result;
	}
	@Override
	public List<String> getInterfaces(String cn)
	{
		logger.logEnter("interfaces " + cn);
		List<String> result = communicator.getInterfaces(cn);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF callConstructor(String cn, List<String> params, List<REF> argRefs)
	{
		logger.logEnter("new " + cn + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		REF result = communicator.callConstructor(cn, params, argRefs);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs)
	{
		logger.logEnter(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		REF result = communicator.callStaticMethod(cn, name, returnClassname, params, argRefs);
		logger.logExit(result);
		return result;
	}
	@Override
	public REF getStaticField(String cn, String name, String fieldClassname)
	{
		logger.logEnter(fieldClassname + " " + cn + "." + name);
		REF result = communicator.getStaticField(cn, name, fieldClassname);
		logger.logExit(result);
		return result;
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef)
	{
		logger.logEnter(fieldClassname + " " + cn + "." + name + " = " + valueRef);
		communicator.setStaticField(cn, name, fieldClassname, valueRef);
		logger.logExit();
	}

	@Override
	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs)
	{
		logger.logEnter(returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
		REF result = communicator.callInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
		logger.logExit(result);
		return result;
	}
	@Override
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef)
	{
		logger.logEnter(fieldClassname + " " + cn + "." + name + ": " + receiverRef);
		REF result = communicator.getInstanceField(cn, name, fieldClassname, receiverRef);
		logger.logExit(result);
		return result;
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef)
	{
		logger.logEnter(fieldClassname + " " + cn + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(cn, name, fieldClassname, receiverRef, valueRef);
		logger.logExit();
	}

	@Override
	public TC getTransceiver()
	{
		return loggingTransceiver;
	}

	@Override
	public CM getCallbackManager()
	{
		return loggingCallbackManager;
	}
}
