package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class WeakSameJVMRefManager<ATTACHMENT> implements SameJVMRefManager<ATTACHMENT>
{
	/**
	 * We want need a weak, concurrent identity-based map.
	 * Sadly, we have to choose between {@link WeakHashMap}, {@link ConcurrentHashMap}, or {@link IdentityHashMap}.
	 * Implementing weak references isn't hard, so we don't use {@link WeakHashMap}.
	 * The overhead of wrapping each object seems smaller than the overhead of having to synchronize on every map access.
	 * (When using an unsynchronized map, we can't even call get unsyncrhonized in a fast path since the map might be in an invalid state,
	 * or worse, change state while get is running.)
	 * So, we use a {@link ConcurrentHashMap} (concurrent) mapping {@link IdentityObjectContainer} (identity-based) to {@link WeakReference}s (weak).
	 */
	private final ConcurrentHashMap<IdentityObjectContainer, WeakReference<SameJVMRef<ATTACHMENT>>> cachedRefs;

	public WeakSameJVMRefManager()
	{
		cachedRefs = new ConcurrentHashMap<>();
	}

	@Override
	public SameJVMRef<ATTACHMENT> pack(Object obj)
	{
		if(obj == null)
			return null;

		// ouch... but see comment on cachedRefs
		IdentityObjectContainer container = new IdentityObjectContainer(obj);

		// We can't really use computeIfAbsent:
		// The returned WeakReference might be null, in which case we have to recreate a SameJVMRef as well.
		// We can't express this using computeIfAbsent.
		// Also, inside the mapping function, we have to wrap a newly created SameJVMRef in a WeakReference
		// before passing it to the map. So, the ref will (for a short while) only be accessible through the WeakReference.
		// The JVM could optimize this away to immediately delete the SameJVMRef if the mapping function finishes.

		// fast path
		WeakReference<SameJVMRef<ATTACHMENT>> weakRef = cachedRefs.get(container);
		if(weakRef != null)
		{
			SameJVMRef<ATTACHMENT> ref = weakRef.get();
			if(ref != null)
				return ref;
		}

		// slow path: fully synchronized
		synchronized(cachedRefs)
		{
			// re-get; maybe another thread was faster
			weakRef = cachedRefs.get(container);
			if(weakRef != null)
			{
				SameJVMRef<ATTACHMENT> ref = weakRef.get();
				if(ref != null)
					return ref;
			}

			// No SameJVMRef for that object anymore. Create a new one.
			SameJVMRef<ATTACHMENT> ref = new SameJVMRef<>(obj);
			cachedRefs.put(container, new WeakReference<SameJVMRef<ATTACHMENT>>(ref));
			return ref;
		}
	}
}
