package net.haspamelodica.charon.utils.maps;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.haspamelodica.charon.utils.maps.suppliers.UnidirectionalMapWithRemovalIteratorSupplier;

// TODO maybe consider switching to phantom refs:
// It's possible for an object to become (strongly) reachable after weak refs to it have been cleared,
// by using finalization.
// This is true even if the weakly reachable object has no finalizer,
// by letting another object stronly refer to the weakly reachable object,
// letting this other object have a finalizer making it strongly reachable again,
// and then letting the garbage collector finalize that object.
// Phantom refs would solve this because after the phantom refs to an object are cleared,
// there's _no_ way to make it reachable again.
public class WeakIdentityUnidirectionalMap<K, V> extends AbstractUnidirectionalMap<K, V>
{
	private final UnidirectionalMapWithRemovalIterator<Integer, List<WeakIdentityEntry<K, V>>> map;

	private final ReferenceQueue<K> refqueue;

	public WeakIdentityUnidirectionalMap(UnidirectionalMapWithRemovalIteratorSupplier mapSupplier)
	{
		this.map = mapSupplier.createMap();
		this.refqueue = new ReferenceQueue<>();
	}

	@Override
	public boolean containsKey(K key)
	{
		return checkAndDoMaintenanceAndLookupEntry(key, false).entryIfFound() != null;
	}

	@Override
	public V get(K key)
	{
		FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound = checkAndDoMaintenanceAndLookupEntry(key, false).entryIfFound();
		return entryIfFound != null ? entryIfFound.element().value() : null;
	}

	@Override
	public V put(K key, V value)
	{
		LookupResult<K, V> lookupResult = checkAndDoMaintenanceAndLookupEntry(key, true);

		FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound = lookupResult.entryIfFound();
		if(entryIfFound != null)
			return lookupResult.entryIfFound().element().setValueAndGetOldValue(value);

		lookupResult.entries().add(new WeakIdentityEntry<>(key, value, refqueue));
		return null;
	}

	@Override
	public V remove(K key)
	{
		FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound = checkAndDoMaintenanceAndLookupEntry(key, false).entryIfFound();
		if(entryIfFound == null)
			return null;

		entryIfFound.iterator().remove();
		return entryIfFound.element().value();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
	{
		LookupResult<K, V> lookupResult = checkAndDoMaintenanceAndLookupEntry(key, true);

		FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound = lookupResult.entryIfFound();
		if(entryIfFound != null)
			return lookupResult.entryIfFound().element().value();

		V value = mappingFunction.apply(key);
		lookupResult.entries().add(new WeakIdentityEntry<>(key, value, refqueue));
		return value;
	}

	private LookupResult<K, V> checkAndDoMaintenanceAndLookupEntry(K key, boolean createEntriesListIfAbsent)
	{
		Objects.requireNonNull(key);
		removeClearedKeys();

		int hashCode = System.identityHashCode(key);

		List<WeakIdentityEntry<K, V>> entries = createEntriesListIfAbsent
				? map.computeIfAbsent(hashCode, h -> new ArrayList<>())
				: map.get(hashCode);

		if(entries == null)
			return new LookupResult<>(null, null);

		FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound = findFirstMatching(entries, e -> e.refersToKey(key));
		return new LookupResult<>(entryIfFound, entries);
	}
	private static record LookupResult<K, V>(FirstMatchingResult<WeakIdentityEntry<K, V>> entryIfFound, List<WeakIdentityEntry<K, V>> entries)
	{}

	@Override
	public void removeIf(BiPredicate<K, V> removalPredicate)
	{
		Objects.requireNonNull(removalPredicate);
		removeClearedKeys();

		for(Iterator<Entry<Integer, List<WeakIdentityEntry<K, V>>>> iteratorPerHashCode = map.iterator(); iteratorPerHashCode.hasNext();)
		{
			List<WeakIdentityEntry<K, V>> entries = iteratorPerHashCode.next().value();
			for(Iterator<WeakIdentityEntry<K, V>> iteratorPerEntry = entries.iterator(); iteratorPerEntry.hasNext();)
			{
				WeakIdentityEntry<K, V> entry = iteratorPerEntry.next();
				K key = entry.getKeyOrNull();
				if(key == null)
					iteratorPerEntry.remove();
				else if(removalPredicate.test(key, entry.value()))
					iteratorPerEntry.remove();
			}
			if(entries.isEmpty())
				iteratorPerHashCode.remove();
		}
	}

	@Override
	public Stream<Entry<K, V>> stream()
	{
		removeClearedKeys();

		return map
				.stream()
				.map(Entry::value)
				.flatMap(List::stream)
				.map(e -> new Entry<>(e.getKeyOrNull(), e.value()))
				.filter(e -> e.key() != null);
	}

	private void removeClearedKeys()
	{
		for(;;)
		{
			@SuppressWarnings("unchecked") // we only create such refs with the given refqueue
			WeakReferenceWithIdentityHashCode<K> clearedRef = (WeakReferenceWithIdentityHashCode<K>) refqueue.poll();
			if(clearedRef == null)
				break;

			int hashCode = clearedRef.getReferentIdentityHashCode();
			List<WeakIdentityEntry<K, V>> entries = map.get(hashCode);
			FirstMatchingResult<WeakIdentityEntry<K, V>> clearedEntry = findFirstMatching(entries, e -> e.ref() == clearedRef);
			if(clearedEntry != null)
			{
				clearedEntry.iterator().remove();
				if(entries.isEmpty())
					map.remove(hashCode);
			}
		}
	}

	private static <E> FirstMatchingResult<E> findFirstMatching(List<E> list, Predicate<E> predicate)
	{
		for(Iterator<E> iterator = list.iterator(); iterator.hasNext();)
		{
			E element = iterator.next();
			if(predicate.test(element))
				return new FirstMatchingResult<E>(element, iterator);
		}
		return null;
	}
	private static record FirstMatchingResult<E>(E element, Iterator<E> iterator)
	{}

	private static class WeakIdentityEntry<K, V>
	{
		private final WeakReferenceWithIdentityHashCode<K> ref;

		private V value;

		public WeakIdentityEntry(K key, V value, ReferenceQueue<? super K> refqueue)
		{
			this.ref = new WeakReferenceWithIdentityHashCode<>(key, refqueue);
			this.value = value;
		}

		public K getKeyOrNull()
		{
			return ref.get();
		}
		public V value()
		{
			return value;
		}
		public WeakReferenceWithIdentityHashCode<K> ref()
		{
			return ref;
		}
		public boolean refersToKey(K key)
		{
			return ref.refersTo(key);
		}

		public V setValueAndGetOldValue(V value)
		{
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}
	}

	private static class WeakReferenceWithIdentityHashCode<T> extends WeakReference<T>
	{
		private final int hashCode;

		public WeakReferenceWithIdentityHashCode(T referent, ReferenceQueue<? super T> refqueue)
		{
			super(referent, refqueue);
			this.hashCode = System.identityHashCode(referent);
		}

		public int getReferentIdentityHashCode()
		{
			return hashCode;
		}
	}
}
