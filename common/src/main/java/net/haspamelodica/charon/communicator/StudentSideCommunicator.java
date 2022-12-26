package net.haspamelodica.charon.communicator;

import java.util.List;

import net.haspamelodica.charon.refs.Ref;

public interface StudentSideCommunicator
{
	public String getStudentSideClassname(Ref ref);

	public Ref callConstructor(String cn, List<String> params, List<Ref> argRefs);

	public Ref callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<Ref> argRefs);
	public Ref getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, Ref valueRef);

	public Ref callInstanceMethod(String cn, String name, String returnClassname, List<String> params, Ref receiverRef, List<Ref> argRefs);
	public Ref getInstanceField(String cn, String name, String fieldClassname, Ref receiverRef);
	public void setInstanceField(String cn, String name, String fieldClassname, Ref receiverRef, Ref valueRef);

	public Ref createCallbackInstance(String interfaceName, Callback callback);
}
