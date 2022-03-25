package net.haspamelodica.studentcodeseparator.refs.direct;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public final class WeakDirectRefManager<REFERRER> implements DirectRefManager<REFERRER>
{
	/**
	 * We want need a weak, concurrent identity-based map.
	 * Sadly, we have to choose between {@link WeakHashMap}, {@link ConcurrentHashMap}, or {@link IdentityHashMap}.
	 * Implementing weak references isn't hard, so we don't use {@link WeakHashMap}.
	 * The overhead of wrapping each object seems smaller than the overhead of having to synchronize on every map access.
	 * (When using an unsynchronized map, we can't even call get unsyncrhonized in a fast path since the map might be in an invalid state,
	 * or worse, change state while get is running.)
	 * So, we use a {@link ConcurrentHashMap} (concurrent)
	 * mapping {@link IdentityObjectContainer}(identity-based) to {@link WeakReference}s (weak).
	 */
	private final ConcurrentHashMap<IdentityObjectContainer,
			WeakReferenceWithAttachment<IdentityObjectContainer, Ref<Object, REFERRER>>> cachedRefs;

	private final ReferenceQueue<Ref<Object, REFERRER>> queue;

	public WeakDirectRefManager()
	{
		this.cachedRefs = new ConcurrentHashMap<>();
		this.queue = new ReferenceQueue<>();
	}

	@Override
	public Ref<Object, REFERRER> pack(Object obj)
	{
		if(obj == null)
			return null;

		pollAndHandleQueue();

		// ouch... but see comment on cachedRefs
		IdentityObjectContainer container = new IdentityObjectContainer(obj);

		// We can't really use computeIfAbsent:
		// The returned WeakReference might be null, in which case we have to recreate a DirectRef as well.
		// We can't express this using computeIfAbsent.
		// Also, inside the mapping function, we have to wrap a newly created DirectRef in a WeakReference
		// before passing it to the map. So, the ref will (for a short while) only be accessible through the WeakReference.
		// The JVM could optimize this away to immediately delete the DirectRef if the mapping function finishes.

		// fast path
		WeakReferenceWithAttachment<IdentityObjectContainer, Ref<Object, REFERRER>> weakRef = cachedRefs.get(container);
		if(weakRef != null)
		{
			Ref<Object, REFERRER> ref = weakRef.get();
			// Yes, we polled the queue, but some object could have been cleared since then
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
				Ref<Object, REFERRER> ref = weakRef.get();
				if(ref != null)
					return ref;
			}

			// No ref for that object anymore. Create a new one.
			Ref<Object, REFERRER> ref = new Ref<>(obj);
			cachedRefs.put(container, new WeakReferenceWithAttachment<>(ref, container, queue));
			return ref;
		}
	}

	private void pollAndHandleQueue()
	{
		for(;;)
		{
			@SuppressWarnings("unchecked")
			WeakReferenceWithAttachment<IdentityObjectContainer, Ref<Object, REFERRER>> clearedRef =
					(WeakReferenceWithAttachment<IdentityObjectContainer, Ref<Object, REFERRER>>) queue.poll();
			if(clearedRef == null)
				break;
			cachedRefs.remove(clearedRef.attachment());
		}
	}
}
