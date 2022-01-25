package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.util.List;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class SameJVMRef implements Ref
{
	private final Object obj;

	private StudentSideInstance studentSideInstance;

	public SameJVMRef(Object obj)
	{
		this.obj = obj;
	}

	@Override
	public StudentSideInstance getStudentSideInstance()
	{
		return studentSideInstance;
	}
	@Override
	public void setStudentSideInstance(StudentSideInstance studentSideInstance)
	{
		this.studentSideInstance = studentSideInstance;
	}

	public Object obj()
	{
		return obj;
	}

	public static List<Object> unpack(List<SameJVMRef> refs)
	{
		return refs.stream().map(SameJVMRef::unpack).toList();
	}
	public static List<SameJVMRef> pack(List<?> objs)
	{
		return objs.stream().map(SameJVMRef::pack).toList();
	}

	public static Object unpack(SameJVMRef ref)
	{
		return ref.obj();
	}
	public static SameJVMRef pack(Object obj)
	{
		return new SameJVMRef(obj);
	}
}
