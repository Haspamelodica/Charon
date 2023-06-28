package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;

public class LoggingStudentSideCommunicatorCallbacks<REF, THROWABLEREF extends REF, TYPEREF extends REF>
		implements StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF>
{
	private final CommunicationLogger<REF, TYPEREF, ?, ?, ?> logger;

	private final StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF> callbacks;

	public LoggingStudentSideCommunicatorCallbacks(CommunicationLogger<REF, TYPEREF, ?, ?, ?> logger,
			StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF> callbacks)
	{
		this.logger = logger;
		this.callbacks = callbacks;
	}

	@Override
	public String getCallbackInterfaceCn(REF ref)
	{
		logger.logEnterCallback("callback interface " + r(ref));
		String result = callbacks.getCallbackInterfaceCn(ref);
		logger.logExitCallback(result);
		return result;
	}

	@Override
	public CallbackOperationOutcome<REF, THROWABLEREF> callCallbackInstanceMethod(
			TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs)
	{
		logger.logEnterCallback("callback " + t(returnType) + " " + t(type) + "." + name + t(params) + ": " + r(receiverRef) + ", " + r(argRefs));
		CallbackOperationOutcome<REF, THROWABLEREF> result = callbacks.callCallbackInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
		logger.logExitCallback(c(result));
		return result;
	}

	private String c(CallbackOperationOutcome<REF, THROWABLEREF> outcome)
	{
		return logger.callbackOutcomeToString(outcome);
	}

	protected String t(List<TYPEREF> typerefs)
	{
		return logger.typerefsToString(typerefs);
	}

	protected String t(TYPEREF typeref)
	{
		return logger.typerefToString(typeref);
	}

	private String r(List<REF> refs)
	{
		return logger.refsToString(refs);
	}

	private String r(REF ref)
	{
		return logger.refToString(ref);
	}
}
