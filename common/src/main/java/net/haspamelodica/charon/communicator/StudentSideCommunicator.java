package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicator<REF extends Ref<?, ?>>
{
	public String getStudentSideClassname(REF ref);

	public REF callConstructor(String cn, List<String> params, List<REF> argRefs);

	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs);
	public REF getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef);

	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef);
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef);

	public REF createCallbackInstance(String interfaceName, Callback<REF> callback);
}
