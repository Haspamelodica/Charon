package net.haspamelodica.studentcodeseparator.communicator;

import java.util.List;

import net.haspamelodica.studentcodeseparator.serialization.Serializer;

/**
 * There must not be a REF referring to a null value.
 * If null occurs student-side, the exercise-side REF must also be null.
 */
public interface StudentSideCommunicator<REF>
{
	public String getStudentSideClassname(REF ref);

	public <T> REF send(Serializer<T> serializer, REF serializerRef, T obj);
	public <T> T receive(Serializer<T> serializer, REF serializerRef, REF objRef);

	public REF callConstructor(String cn, List<String> params, List<REF> argRefs);

	public REF callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF> argRefs);
	public REF getStaticField(String cn, String name, String fieldClassname);
	public void setStaticField(String cn, String name, String fieldClassname, REF valueRef);

	public REF callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF receiverRef, List<REF> argRefs);
	public REF getField(String cn, String name, String fieldClassname, REF receiverRef);
	public void setField(String cn, String name, String fieldClassname, REF receiverRef, REF valueRef);
}
