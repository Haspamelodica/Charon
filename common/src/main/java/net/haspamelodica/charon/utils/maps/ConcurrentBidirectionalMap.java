package net.haspamelodica.charon.utils.maps;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.BidirectionalMapSupplier;

public class ConcurrentBidirectionalMap<K, V> extends AbstractBidirectionalMap<K, V>
{
	private final BidirectionalMap<K, V> map;

	public ConcurrentBidirectionalMap(BidirectionalMapSupplier mapSupplier)
	{
		this.map = mapSupplier.createMap();
	}

	@Override
	public boolean containsKey(K key)
	{
		synchronized(map)
		{
			return map.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(V value)
	{
		synchronized(map)
		{
			return map.containsValue(value);
		}
	}

	@Override
	public V getValue(K key)
	{
		synchronized(map)
		{
			return map.getValue(key);
		}
	}

	@Override
	public K getKey(V value)
	{
		synchronized(map)
		{
			return map.getKey(value);
		}
	}

	@Override
	public V computeValueIfAbsent(K key, Function<K, V> createValue)
	{
		synchronized(map)
		{
			return map.computeValueIfAbsent(key, createValue);
		}
	}

	@Override
	public K computeKeyIfAbsent(V value, Function<V, K> createKey)
	{
		synchronized(map)
		{
			return map.computeKeyIfAbsent(value, createKey);
		}
	}

	@Override
	public void put(K key, V value)
	{
		synchronized(map)
		{
			map.put(key, value);
		}
	}

	@Override
	public V removeByKey(K key)
	{
		synchronized(map)
		{
			return map.removeByKey(key);
		}
	}

	@Override
	public K removeByValue(V value)
	{
		synchronized(map)
		{
			return map.removeByValue(value);
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
