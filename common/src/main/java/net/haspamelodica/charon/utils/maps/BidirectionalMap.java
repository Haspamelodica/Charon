package net.haspamelodica.charon.utils.maps;

import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.BidirectionalMapSupplier;

public interface BidirectionalMap<K, V>
{
	public V getValue(K key);
	public K getKey(V value);
	public V computeValueIfAbsent(K key, Function<K, V> createValue);
	public K computeKeyIfAbsent(V value, Function<V, K> createKey);
	public void put(K key, V value);
	public V removeByKey(K key);
	public K removeByValue(V value);

	public Stream<Entry<K, V>> stream();
	public default Stream<K> keyStream()
	{
		return stream().map(Entry::key);
	}
	public default Stream<V> valueStream()
	{
		return stream().map(Entry::value);
	}

	public default <R> R computeWithStream(Function<Stream<Entry<K, V>>, R> function)
	{
		return function.apply(stream());
	}
	public default <R> R computeWithKeyStream(Function<Stream<K>, R> function)
	{
		return computeWithStream(s -> function.apply(s.map(Entry::key)));
	}
	public default <R> R computeWithValueStream(Function<Stream<V>, R> function)
	{
		return computeWithStream(s -> function.apply(s.map(Entry::value)));
	}

	// Sadly, we can't override the real toString from an interface.
	public default String defaultToString()
	{
		return computeWithStream(UnidirectionalMap::defaultToString);
	}

	public static Builder builder()
	{
		return new Builder();
	}
	public static class Builder
	{
		private final UnidirectionalMap.Builder	keyToValuesBuilder;
		private final UnidirectionalMap.Builder	valuesToKeysBuilder;

		private boolean concurrent;

		private Builder()
		{
			keyToValuesBuilder = UnidirectionalMap.builder();
			valuesToKeysBuilder = UnidirectionalMap.builder();
		}

		public Builder identityKeys()
		{
			return identityKeys(true);
		}
		public Builder identityKeys(boolean identityKeys)
		{
			keyToValuesBuilder.identityMap(identityKeys);
			return this;
		}

		public Builder identityValues()
		{
			return identityValues(true);
		}
		public Builder identityValues(boolean identityValues)
		{
			valuesToKeysBuilder.identityMap(identityValues);
			return this;
		}

		public Builder weakKeys()
		{
			return weakKeys(true);
		}
		public Builder weakKeys(boolean weakKeys)
		{
			//TODO debug if doing this separately in both uni maps causes problems
			keyToValuesBuilder.weakKeys(weakKeys);
			valuesToKeysBuilder.weakValues(weakKeys);
			return this;
		}

		public Builder weakValues()
		{
			return weakValues(true);
		}

		public Builder weakValues(boolean weakValues)
		{
			//TODO debug if doing this separately in both uni maps causes problems
			keyToValuesBuilder.weakValues(weakValues);
			valuesToKeysBuilder.weakKeys(weakValues);
			return this;
		}

		public Builder concurrent()
		{
			return concurrent(true);
		}
		public Builder concurrent(boolean concurrent)
		{
			// Don't delegate to "sub-maps".
			this.concurrent |= concurrent;
			return this;
		}

		public <K, V> BidirectionalMap<K, V> build()
		{
			BidirectionalMapSupplier supplier = this::buildExceptConcurrency;
			return concurrent ? new ConcurrentBidirectionalMap<>(supplier) : supplier.createMap();
		}
		private <K, V> BidirectionalMap<K, V> buildExceptConcurrency()
		{
			return new BidirectionalMapImpl<>(keyToValuesBuilder::build, valuesToKeysBuilder::build);
		}
	}
}
