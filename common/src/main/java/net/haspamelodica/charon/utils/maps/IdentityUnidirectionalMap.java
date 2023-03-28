package net.haspamelodica.charon.utils.maps;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

public class IdentityUnidirectionalMap<K, V> extends AbstractUnidirectionalMap<K, V>
{
	private final UnidirectionalMap<IdentityReference<K>, V> map;

	public IdentityUnidirectionalMap(UnidirectionalMapSupplier mapSupplier)
	{
		this.map = mapSupplier.createMap();
	}

	@Override
	public boolean containsKey(K key)
	{
		return map.containsKey(new IdentityReference<>(key));
	}

	@Override
	public V get(K key)
	{
		return map.get(new IdentityReference<>(key));
	}

	@Override
	public V put(K key, V value)
	{
		return map.put(new IdentityReference<>(key), value);
	}

	@Override
	public V remove(K key)
	{
		return map.remove(new IdentityReference<>(key));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		return map.computeIfAbsent(new IdentityReference<>(key), r -> mappingFunction.apply(r.t()));
	}

	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		map.removeIf((k, v) -> removalPredicate.test(k.t(), v));
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		return map.stream().map(e -> new Entry<>(e.key().t(), e.value()));
	}

	private static record IdentityReference<T>(T t)
	{
		@Override
		public int hashCode()
		{
			return System.identityHashCode(t);
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(!(obj instanceof IdentityReference<?> other))
				return false;
			return t == other.t;
		}
	}
}
