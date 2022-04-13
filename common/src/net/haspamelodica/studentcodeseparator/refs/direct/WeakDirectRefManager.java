package net.haspamelodica.studentcodeseparator.refs.direct;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public final class WeakDirectRefManager<REF extends Ref<Object, ?>> implements DirectRefManager<REF>
{
	/**
	 * We want need a concurrent identity-based map with weak values.
	 * Sadly, we have to choose between {@link ConcurrentHashMap}, or {@link IdentityHashMap}.
	 * ({@link WeakHashMap} has weak keys, not weak values.)
	 * The overhead of wrapping each object seems smaller than the overhead of having to synchronize on every map access.
	 * (When using an unsynchronized map, we can't even call get unsynchronized in a fast path since the map might be in an invalid state,
	 * or worse, change state while get is running.)
	 * So, we use a {@link ConcurrentHashMap} (concurrent)
	 * mapping {@link IdentityObjectContainer}(identity-based) to {@link WeakReference}s (weak values).
	 */
	private final ConcurrentHashMap<IdentityObjectContainer,
			WeakReferenceWithAttachment<IdentityObjectContainer, REF>> cachedRefs;

	private final ReferenceQueue<REF> queue;

	public WeakDirectRefManager()
	{
		this.cachedRefs = new ConcurrentHashMap<>();
		this.queue = new ReferenceQueue<>();
	}

	@Override
	public REF pack(Object obj)
	{
		if(obj == null)
			return null;

		//TODO Calling this really doesn't belong in pack()...
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
		WeakReferenceWithAttachment<IdentityObjectContainer, REF> weakRef = cachedRefs.get(container);
		if(weakRef != null)
		{
			REF ref = weakRef.get();
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
				REF ref = weakRef.get();
				if(ref != null)
					return ref;
			}

			// No ref for that object anymore. Create a new one.
			//TODO to fix this unchecked cast, we either have to replace all uses of REF in all classes with Ref<concrete type arguments...>
			// or pass Ref constructors down the entire hierarchy. Same in IntRefManager.
			REF ref = (REF) new Ref<>(obj);
			cachedRefs.put(container, new WeakReferenceWithAttachment<>(ref, container, queue));
			return ref;
		}
	}

	private void pollAndHandleQueue()
	{
		for(;;)
		{
			@SuppressWarnings("unchecked")
			WeakReferenceWithAttachment<IdentityObjectContainer, REF> clearedRef =
					(WeakReferenceWithAttachment<IdentityObjectContainer, REF>) queue.poll();
			if(clearedRef == null)
				break;
			cachedRefs.remove(clearedRef.attachment());
		}
	}
}
