package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.util.List;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class SameJVMRef extends Ref
{
	//	private static final IdentityHashMap<Object, WeakReference<SameJVMRef>> cachedRefs;

	private final Object obj;

	private SameJVMRef(Object obj)
	{
		this.obj = obj;
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
		return ref == null ? null : ref.obj();
	}
	public static SameJVMRef pack(Object obj)
	{
		//TODO only create one SameJVMRef per object
		return obj == null ? null : new SameJVMRef(obj);
	}

	@Override
	public String toString()
	{
		return "Ref[" + obj.toString() + "]";
	}
}
