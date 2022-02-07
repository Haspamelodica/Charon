package net.haspamelodica.studentcodeseparator.communicator;

import java.util.List;

public interface StudentSideCommunicatorWithoutSerialization<ATTACHMENT, REF extends Ref<ATTACHMENT>>
{
	public String getStudentSideClassname(REF ref);

	public REF callConstructor(String cn, List<String> params, List<REF> argRefs);

	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs);
	public REF getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef);

	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef);
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef);
}
