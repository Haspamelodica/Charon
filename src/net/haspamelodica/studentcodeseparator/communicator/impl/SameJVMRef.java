package net.haspamelodica.studentcodeseparator.communicator.impl;

import java.util.List;

public record SameJVMRef(Object obj)
{
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
