package net.haspamelodica.charon.utils.maps;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

public class ConcurrentUnidirectionalMap<K, V> extends AbstractUnidirectionalMap<K, V>
{
	private final UnidirectionalMap<K, V> map;

	public ConcurrentUnidirectionalMap(UnidirectionalMapSupplier mapSupplier)
	{
		map = mapSupplier.createMap();
	}

	@Override
	public V get(K key)
	{
		synchronized(map)
		{
			return map.get(key);
		}
	}

	@Override
	public V put(K key, V value)
	{
		synchronized(map)
		{
			return map.put(key, value);
		}
	}

	@Override
	public V remove(K key)
	{
		synchronized(map)
		{
			return map.remove(key);
		}
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		synchronized(map)
		{
			return map.computeIfAbsent(key, mappingFunction);
		}
	}

	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		synchronized(map)
		{
			map.removeIf(removalPredicate);
		}
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		//TODO can we do better?
		List<Entry<K, V>> copy;
		synchronized(map)
		{
			copy = map.computeWithStream(Stream::toList);
		}
		return copy.stream();
	}
	@Override
	public <R> R computeWithStream(Function<Stream<Entry<K, V>>, R> function)
	{
		synchronized(map)
		{
			return map.computeWithStream(function);
		}
	}
}
