package net.haspamelodica.charon.utils.maps;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.MapSupplier;

public class UnidirectionalMapImpl<K, V> extends AbstractUnidirectionalMap<K, V> implements UnidirectionalMapWithRemovalIterator<K, V>
{
	private final Map<K, V> map;

	public UnidirectionalMapImpl(MapSupplier mapSupplier)
	{
		this.map = mapSupplier.createMap();
	}

	@Override
	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}

	@Override
	public V get(K key)
	{
		return map.get(key);
	}
	@Override
	public V put(K key, V value)
	{
		return map.put(key, value);
	}
	@Override
	public V remove(K key)
	{
		return map.remove(key);
	}
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		return map.computeIfAbsent(key, mappingFunction);
	}
	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		map.entrySet().removeIf(e -> removalPredicate.test(e.getKey(), e.getValue()));
	}
	@Override
	public Stream<Entry<K, V>> stream()
	{
		return map.entrySet().stream().map(e -> new Entry<>(e.getKey(), e.getValue()));
	}

	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new Iterator<>()
		{
			private final Iterator<java.util.Map.Entry<K, V>> iterator = map.entrySet().iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entry<K, V> next()
			{
				java.util.Map.Entry<K, V> entry = iterator.next();
				return new Entry<>(entry.getKey(), entry.getValue());
			}

			@Override
			public void remove()
			{
				iterator.remove();
			}
		};
	}
}
