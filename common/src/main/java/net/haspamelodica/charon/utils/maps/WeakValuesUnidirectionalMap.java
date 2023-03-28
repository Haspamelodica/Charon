package net.haspamelodica.charon.utils.maps;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapSupplier;

// TODO debug how this map behaves for weak keys
public class WeakValuesUnidirectionalMap<K, V> extends AbstractUnidirectionalMap<K, V>
{
	private final UnidirectionalMap<K, WeakReference<V>>		map;
	private final UnidirectionalMap<Reference<? extends V>, K>	refToKeyMap;
	private final ReferenceQueue<V>								refqueue;

	public WeakValuesUnidirectionalMap(UnidirectionalMapSupplier mapSupplier, UnidirectionalMapSupplier weakValueIfWeakKeyMapSupplier)
	{
		this.map = mapSupplier.createMap();
		this.refToKeyMap = weakValueIfWeakKeyMapSupplier.createMap();
		this.refqueue = new ReferenceQueue<>();
	}

	@Override
	public boolean containsKey(K key)
	{
		pollRefqueue();

		return map.containsKey(key);
	}

	@Override
	public V get(K key)
	{
		pollRefqueue();

		WeakReference<V> ref = map.get(key);
		if(ref == null)
			// happens if the key doesn't exist
			return null;
		// It isn't a problem if the value gets reclaimed after the call to pollRefqueue, but before WeakRef.get.
		// This is because we return null in both cases, as expected.
		return ref.get();
	}

	@Override
	public V put(K key, V value)
	{
		Objects.requireNonNull(value);
		pollRefqueue();

		WeakReference<V> ref = new WeakReference<>(value, refqueue);
		WeakReference<V> oldRef = map.put(key, ref);
		refToKeyMap.put(ref, key);

		if(oldRef == null)
			return null;

		refToKeyMap.remove(oldRef);
		// It isn't a problem if the value gets reclaimed after the call to pollRefqueue, but before WeakRef.get.
		// This is because we return null in both cases, as expected.
		return oldRef.get();
	}

	@Override
	public V remove(K key)
	{
		pollRefqueue();

		WeakReference<V> oldRef = map.remove(key);

		if(oldRef == null)
			return null;

		refToKeyMap.remove(oldRef);
		// It isn't a problem if the value gets reclaimed after the call to pollRefqueue, but before WeakRef.get.
		// This is because we return null in both cases, as expected.
		return oldRef.get();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		pollRefqueue();

		// We could try to optimize this by using map.computeIfAbsent, but that seems like premature optimization for now.
		V value = get(key);
		if(value != null)
			return value;

		value = mappingFunction.apply(key);
		put(key, value);
		return value;
	}

	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		pollRefqueue();

		map.removeIf((k, vRef) ->
		{
			V v = vRef.get();
			if(v == null)
			{
				// Got cleared after polling refqueue, but before reaching here
				refToKeyMap.remove(vRef);
				return true;
			}

			if(!removalPredicate.test(k, v))
				return false;

			refToKeyMap.remove(vRef);
			return true;
		});
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		pollRefqueue();

		return map
				.stream()
				.map(e -> new Entry<>(e.key(), e.value().get()))
				.filter(e -> e.value() != null);
	}

	private void pollRefqueue()
	{
		for(;;)
		{
			Reference<? extends V> clearedRef = refqueue.poll();
			if(clearedRef == null)
				break;
			K key = refToKeyMap.remove(clearedRef);
			if(key == null)
				// happens if a reference to a value which has since been overwritten gets cleared,
				// or if we are a map with weak keys and the key has been cleared already.
				continue;
			map.remove(key);
		}
	}
}
