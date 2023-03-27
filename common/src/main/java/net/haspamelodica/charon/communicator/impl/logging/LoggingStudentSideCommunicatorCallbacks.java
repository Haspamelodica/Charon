package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;

public class LoggingStudentSideCommunicatorCallbacks<REF, CB extends StudentSideCommunicatorCallbacks<REF>>
		implements StudentSideCommunicatorCallbacks<REF>
{
	private final CommunicationLogger logger;

	protected final CB callbacks;

	public LoggingStudentSideCommunicatorCallbacks(CommunicationLogger logger, CB callbacks)
	{
		this.logger = logger;
		this.callbacks = callbacks;
	}

	@Override
	public String getCallbackInterfaceCn(REF ref)
	{
		logger.logEnterCallback("callback interface " + ref);
		String result = callbacks.getCallbackInterfaceCn(ref);
		logger.logExitCallback(result);
		return result;
	}

	@Override
	public RefOrError<REF> callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			REF receiverRef, List<REF> argRefs)
	{
		logger.logEnterCallback("callback " + returnClassname + " " + cn + "." + name + params.stream().collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
		RefOrError<REF> result = callbacks.callCallbackInstanceMethod(cn, name, returnClassname, params, receiverRef, argRefs);
		logger.logExitCallback(result);
		return result;
	}
}
