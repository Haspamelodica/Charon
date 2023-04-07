package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;

public class LoggingStudentSideCommunicatorCallbacks<REF, TYPEREF extends REF>
		implements StudentSideCommunicatorCallbacks<REF, TYPEREF>
{
	private final CommunicationLogger<TYPEREF> logger;

	private final StudentSideCommunicatorCallbacks<REF, TYPEREF> callbacks;

	public LoggingStudentSideCommunicatorCallbacks(CommunicationLogger<TYPEREF> logger, StudentSideCommunicatorCallbacks<REF, TYPEREF> callbacks)
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
	public RefOrError<REF> callCallbackInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
			REF receiverRef, List<REF> argRefs)
	{
		logger.logEnterCallback("callback " + t(returnType) + " " + t(type) + "." + name
				+ params.stream().map(this::t).collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
		RefOrError<REF> result = callbacks.callCallbackInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
		logger.logExitCallback(result);
		return result;
	}

	protected String t(TYPEREF typeref)
	{
		return logger.typerefToString(typeref);
	}
}
