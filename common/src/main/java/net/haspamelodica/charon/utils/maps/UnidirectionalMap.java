package net.haspamelodica.charon.utils.maps;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

public interface UnidirectionalMap<K, V>
{
	public V get(K key);
	public V put(K key, V value);
	public V remove(K key);
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

	public void removeIf(BiPredicate<K, V> removalPredicate);

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
	public static <K, V> String defaultToString(Stream<Entry<K, V>> stream)
	{
		return stream
				.map(e -> e.key() + "=" + e.value())
				.collect(Collectors.joining(", ", "{", "}"));
	}

	public static Builder builder()
	{
		return new Builder();
	}
	public static class Builder
	{
		private boolean	identityMap;
		private boolean	weakKeys;
		private boolean	weakValues;
		private boolean	concurrent;

		private Builder()
		{}

		public Builder identityMap()
		{
			return identityMap(true);
		}
		public Builder identityMap(boolean identityMap)
		{
			this.identityMap |= identityMap;
			return this;
		}

		public Builder weakKeys()
		{
			return weakKeys(true);
		}
		public Builder weakKeys(boolean weakKeys)
		{
			this.weakKeys |= weakKeys;
			return this;
		}

		public Builder weakValues()
		{
			return weakValues(true);
		}
		public Builder weakValues(boolean weakValues)
		{
			this.weakValues |= weakValues;
			return this;
		}

		public Builder concurrent()
		{
			return concurrent(true);
		}
		public Builder concurrent(boolean concurrent)
		{
			this.concurrent |= concurrent;
			return this;
		}

		// Has to be split into multiple methods because UnidirectionalMapSupplier's method is generic
		// and thus can be realized with a method reference, but not a lambda.
		public <K, V> UnidirectionalMap<K, V> build()
		{
			UnidirectionalMapSupplier supplier = this::buildConsideringWeakKeysWeakValuesIdentity;
			return concurrent ? new ConcurrentUnidirectionalMap<>(supplier) : supplier.createMap();
		}
		private <K, V> UnidirectionalMap<K, V> buildConsideringWeakKeysWeakValuesIdentity()
		{
			UnidirectionalMapSupplier supplier = this::buildConsideringWeakKeysIdentity;
			return weakValues ? new WeakValuesUnidirectionalMap<>(supplier, this::buildWeakValueIfWeakKeyMapSupplier) : supplier.createMap();
		}
		private <K, V> UnidirectionalMap<K, V> buildWeakValueIfWeakKeyMapSupplier()
		{
			UnidirectionalMapSupplier supplier = this::buildDefault;
			return weakKeys ? new WeakValuesUnidirectionalMap<>(supplier, supplier) : supplier.createMap();
		}
		private <K, V> UnidirectionalMap<K, V> buildConsideringWeakKeysIdentity()
		{
			return weakKeys
					? identityMap
							? new WeakIdentityUnidirectionalMap<>(this::buildDefault)
							: new UnidirectionalMapImpl<>(WeakHashMap::new)
					: identityMap
							? new UnidirectionalMapImpl<>(IdentityHashMap::new)
							: buildDefault();
		}
		private <K, V> UnidirectionalMap<K, V> buildDefault()
		{
			return new UnidirectionalMapImpl<>(HashMap::new);
		}
	}
}
