package net.haspamelodica.charon.utils.maps;

import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

public class BidirectionalMapImpl<K, V> extends AbstractBidirectionalMap<K, V>
{
	private final UnidirectionalMap<K, V>	keysToValues;
	private final UnidirectionalMap<V, K>	valuesToKeys;

	public BidirectionalMapImpl(UnidirectionalMapSupplier keyMapSupplier, UnidirectionalMapSupplier valueMapSupplier)
	{
		this.keysToValues = keyMapSupplier.createMap();
		this.valuesToKeys = valueMapSupplier.createMap();
	}

	@Override
	public V getValue(K key)
	{
		return keysToValues.get(key);
	}

	@Override
	public K getKey(V value)
	{
		return valuesToKeys.get(value);
	}

	@Override
	public V computeValueIfAbsent(K key, Function<K, V> createValue)
	{
		return keysToValues.computeIfAbsent(key, k ->
		{
			V value = createValue.apply(k);
			valuesToKeys.put(value, key);
			return value;
		});
	}

	@Override
	public K computeKeyIfAbsent(V value, Function<V, K> createKey)
	{
		return valuesToKeys.computeIfAbsent(value, v ->
		{
			K key = createKey.apply(v);
			keysToValues.put(key, value);
			return key;
		});
	}

	@Override
	public void put(K key, V value)
	{
		V oldValue = keysToValues.put(key, value);
		if(oldValue != null)
			valuesToKeys.remove(oldValue);

		K oldKey = valuesToKeys.put(value, key);
		if(oldKey != null)
			keysToValues.remove(oldKey);
	}

	@Override
	public V removeByKey(K key)
	{
		V value = keysToValues.remove(key);
		if(value != null)
			valuesToKeys.remove(value);
		return value;
	}

	@Override
	public K removeByValue(V value)
	{
		K key = valuesToKeys.remove(value);
		if(key != null)
			keysToValues.remove(key);
		return key;
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		return keysToValues.stream();
	}
}
