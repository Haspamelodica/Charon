package net.haspamelodica.studentcodeseparator.refs;

import java.util.List;
import java.util.function.Function;

public interface DirectRefManager<ATTACHMENT>
{
	public default List<Object> unpack(List<DirectRef<ATTACHMENT>> refs)
	{
		return refs.stream().map(this::unpack).toList();
	}
	public default List<DirectRef<ATTACHMENT>> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, DirectRef<ATTACHMENT>>) this::pack).toList();
	}

	public default Object unpack(DirectRef<ATTACHMENT> ref)
	{
		return ref == null ? null : ref.obj();
	}
	public DirectRef<ATTACHMENT> pack(Object obj);
}
