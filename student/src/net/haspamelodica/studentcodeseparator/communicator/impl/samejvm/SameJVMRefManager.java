package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.util.List;
import java.util.function.Function;

public interface SameJVMRefManager<ATTACHMENT>
{
	public default List<Object> unpack(List<SameJVMRef<ATTACHMENT>> refs)
	{
		return refs.stream().map(this::unpack).toList();
	}
	public default List<SameJVMRef<ATTACHMENT>> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, SameJVMRef<ATTACHMENT>>) this::pack).toList();
	}

	public default Object unpack(SameJVMRef<ATTACHMENT> ref)
	{
		return ref == null ? null : ref.obj();
	}
	public SameJVMRef<ATTACHMENT> pack(Object obj);
}
