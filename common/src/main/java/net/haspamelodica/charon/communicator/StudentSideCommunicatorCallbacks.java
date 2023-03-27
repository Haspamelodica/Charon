package net.haspamelodica.charon.communicator;

import java.util.List;

public interface StudentSideCommunicatorCallbacks<REF>
{
	public String getCallbackInterfaceCn(REF ref);
	public RefOrError<REF> callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
			REF receiverRef, List<REF> argRefs);
}
