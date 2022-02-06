package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.util.List;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class SameJVMRef<ATTACHMENT> extends Ref<ATTACHMENT>
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

	public static <ATTACHMENT> List<Object> unpack(List<SameJVMRef<ATTACHMENT>> refs)
	{
		return refs.stream().map(SameJVMRef::unpack).toList();
	}
	public static <ATTACHMENT> List<SameJVMRef<ATTACHMENT>> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, SameJVMRef<ATTACHMENT>>) SameJVMRef::pack).toList();
	}

	public static <ATTACHMENT> Object unpack(SameJVMRef<ATTACHMENT> ref)
	{
		return ref == null ? null : ref.obj();
	}
	public static <ATTACHMENT> SameJVMRef<ATTACHMENT> pack(Object obj)
	{
		//TODO only create one SameJVMRef per object
		return obj == null ? null : new SameJVMRef<>(obj);
	}

	@Override
	public String toString()
	{
		return "Ref[" + obj.toString() + "]";
	}
}
