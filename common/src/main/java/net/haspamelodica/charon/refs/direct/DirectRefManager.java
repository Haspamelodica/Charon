package net.haspamelodica.charon.refs.direct;

import java.util.List;
import java.util.function.Function;

import net.haspamelodica.charon.refs.Ref;

public interface DirectRefManager
{
	public default List<Object> unpack(List<Ref> refs)
	{
		return refs.stream().map(this::unpack).toList();
	}
	public default List<Ref> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, Ref>) this::pack).toList();
	}

	public default Object unpack(Ref ref)
	{
		return ref == null ? null : ref.referent();
	}
	public Ref pack(Object obj);
}
