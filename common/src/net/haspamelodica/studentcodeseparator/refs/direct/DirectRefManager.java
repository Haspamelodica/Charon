package net.haspamelodica.studentcodeseparator.refs.direct;

import java.util.List;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public interface DirectRefManager<REFERRER>
{
	public default List<Object> unpack(List<Ref<Object, REFERRER>> refs)
	{
		return refs.stream().map(this::unpack).toList();
	}
	public default List<Ref<Object, REFERRER>> pack(List<?> objs)
	{
		return objs.stream().map((Function<Object, Ref<Object, REFERRER>>) this::pack).toList();
	}

	public default Object unpack(Ref<Object, REFERRER> ref)
	{
		return ref == null ? null : ref.referent();
	}
	public Ref<Object, REFERRER> pack(Object obj);
}
