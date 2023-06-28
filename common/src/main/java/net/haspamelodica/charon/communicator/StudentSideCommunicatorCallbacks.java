package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.CallbackOperationOutcome;

public interface StudentSideCommunicatorCallbacks<REF, THROWABLEREF extends REF, TYPEREF extends REF>
{
	//TODO make multiple interfaces possible
	//TODO use TYPEREF instead
	//TODO cache callback methods
	public String getCallbackInterfaceCn(REF ref);
	public CallbackOperationOutcome<REF, THROWABLEREF> callCallbackInstanceMethod(
			TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs);
}
