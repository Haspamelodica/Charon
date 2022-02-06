package net.haspamelodica.studentcodeseparator.communicator;

import java.util.List;

import net.haspamelodica.studentcodeseparator.serialization.Serializer;

/**
 * There must not be a REF referring to a null value.
 * If null occurs student-side, the exercise-side REF must also be null.
 * <p>
 * Comparing REFs with <code>==</code> is equivalent to checking for student-side object identity.
 * In other words, <code>refA == refB</code> is <code>true</code> exactly
 * if <code>refA</code> and <code>refB</code> refer to the same student-side object.
 */
public interface StudentSideCommunicator<ATTACHMENT, REF extends Ref<ATTACHMENT>>
{
	public String getStudentSideClassname(REF ref);

	public <T> REF send(Serializer<T> serializer, REF serializerRef, T obj);
	public <T> T receive(Serializer<T> serializer, REF serializerRef, REF objRef);

	public REF callConstructor(String cn, List<String> params, List<REF> argRefs);

	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs);
	public REF getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef);

	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
	public REF getInstanceField(String cn, String name, String fieldClassname, REF receiverRef);
	public void setInstanceField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef);
}
