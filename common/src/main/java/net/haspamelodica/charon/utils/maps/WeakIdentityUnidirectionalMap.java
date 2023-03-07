package net.haspamelodica.charon.utils.maps;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

// Problem: If a WeakIdentityReference gets cleared, we can't even call hashCode and equals on it reliably.
// For hashCode, we just cache the result, but for equals, that doesn't work.
// Solution: Let equals throw an exception if one of the refs got cleared,
// and then retry all operations as long as this exception is thrown.
// TODO test this entire class!
public class WeakIdentityUnidirectionalMap<K, V> extends AbstractUnidirectionalMap<K, V>
{
	private final UnidirectionalMap<WeakIdentityReference<K>, V> map;

	public WeakIdentityUnidirectionalMap(UnidirectionalMapSupplier mapSupplier)
	{
		this.map = mapSupplier.createMap();
	}

	@Override
	public V get(K key)
	{
		Objects.requireNonNull(key);

		return removeEmptyKeysAndWrapRefClearedException(() -> map.get(new WeakIdentityReference<>(key)));
	}

	@Override
	public V put(K key, V value)
	{
		Objects.requireNonNull(key);

		return removeEmptyKeysAndWrapRefClearedException(() -> map.put(new WeakIdentityReference<>(key), value));
	}

	@Override
	public V remove(K key)
	{
		Objects.requireNonNull(key);

		return removeEmptyKeysAndWrapRefClearedException(() -> map.remove(new WeakIdentityReference<>(key)));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		Objects.requireNonNull(key);

		return removeEmptyKeysAndWrapRefClearedException(() -> map
				.computeIfAbsent(new WeakIdentityReference<>(key), r -> mappingFunction.apply(key)));
	}

	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		removeEmptyKeysAndWrapRefClearedException(() -> map
				.removeIf((kRef, v) ->
				{
					K k = kRef.getOrNull();
					if(k == null)
						return true;
					return removalPredicate.test(k, v);
				}));
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		// We need to avoid RefClearedExceptions being thrown by stream methods after this method returns,
		// so we just make a copy of the entries to be sure the returned stream can't refer to the WeakIdentityReferences anymore.
		return computeWithStream(Stream::toList).stream();
	}

	@Override
	public <R> R computeWithStream(Function<Stream<Entry<K, V>>, R> function)
	{
		return removeEmptyKeysAndWrapRefClearedException(() -> function.apply(map
				.stream()
				.map(e -> new Entry<>(e.key().getOrNull(), e.value()))
				.filter(e -> e.key() != null)));
	}

	private void removeEmptyKeysAndWrapRefClearedException(Runnable action)
	{
		removeEmptyKeysAndWrapRefClearedException(() ->
		{
			action.run();
			return null;
		});
	}
	private <R> R removeEmptyKeysAndWrapRefClearedException(Supplier<R> action)
	{
		for(;;)
			try
			{
				removeClearedKeys();
				return action.get();
			} catch(RefClearedException e)
			{
				// ignore and try again
			}
	}

	private void removeClearedKeys()
	{
		// Can't use a refqueue because removing cleared WeakIdentityReferences from the map would probably cause
		// calls to hashCode and/or equals on the cleared ref.
		map.removeIf((k, v) -> k.isCleared());
	}

	private static class WeakIdentityReference<T>
	{
		private final WeakReference<T>	ref;
		private final int				hashCode;

		public WeakIdentityReference(T t)
		{
			this.ref = new WeakReference<>(t);
			// We need cache this to avoid changing the reported hashCode if we get cleared
			this.hashCode = t.hashCode();
		}

		public boolean isCleared()
		{
			boolean result = ref.get() == null;
			if(result)
				System.out.println("Cleared a ref!");
			return result;
		}

		public T getOrNull()
		{
			return ref.get();
		}
		public T getOrThrow()
		{
			T t = ref.get();
			if(t == null)
				throw new RefClearedException();
			return t;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(!(obj instanceof WeakIdentityReference<?> other))
				return false;
			// We need to throw if our ref is cleard to avoid changing the reported equality between objects
			return this.getOrThrow() == other.getOrThrow();
		}
	}

	private static class RefClearedException extends RuntimeException
	{
		public RefClearedException()
		{
			System.out.println("RefClearedException thrown");
		}
	}
}
