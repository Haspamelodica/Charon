package net.haspamelodica.studentcodeseparator.refs.direct;

import java.util.List;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface DirectRefManager<REF extends Ref<Object, ?, ?, ?, ?, ?>>
{
	public default List<Object> unpack(List<REF> refs)
	{
		return refs.stream().map(this::unpack).toList();
	}
	public default List<REF> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, REF>) this::pack).toList();
	}

	public default Object unpack(REF ref)
	{
		return ref == null ? null : ref.referent();
	}
	public REF pack(Object obj);
}
