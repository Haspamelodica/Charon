package net.haspamelodica.charon.communicator;

import java.util.List;

public interface StudentSideCommunicatorCallbacks<REF, TYPEREF extends REF>
{
	//TODO make multiple interfaces possible
	public String getCallbackInterfaceCn(REF ref);
	public RefOrError<REF> callCallbackInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
			REF receiverRef, List<REF> argRefs);
}
