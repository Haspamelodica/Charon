package net.haspamelodica.charon.communicator;

import java.util.List;

public interface StudentSideCommunicator<REF, TC extends Transceiver, CM extends CallbackManager>
{
	public boolean storeRefsIdentityBased();

	public String getClassname(REF ref);
	public String getSuperclass(String cn);
	public List<String> getInterfaces(String cn);

	public REF callConstructor(String cn, List<String> params, List<REF> argRefs);

	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs);
	public REF getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef);

	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef);
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef);

	public TC getTransceiver();
	public CM getCallbackManager();
}
