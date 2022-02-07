package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

import java.util.concurrent.ConcurrentHashMap;

public final class ManualSameJVMRefManager<ATTACHMENT> implements SameJVMRefManager<ATTACHMENT>
{
	/**
	 * Same considerations as in {@link WeakSameJVMRefManager#cachedRefs}, except we don't have to (or want to) care for weakness.
	 */
	private final ConcurrentHashMap<IdentityObjectContainer, SameJVMRef<ATTACHMENT>> cachedRefs;

	public ManualSameJVMRefManager()
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

		// No need to split into fast and slow paths: We can actually use computeIfAbsent here, since we don't need to be weak.
		// Also, use c.get() instead of obj directly, to make the lambda independent from local variables. Might help otimization.
		return cachedRefs.computeIfAbsent(container, c -> new SameJVMRef<>(c.get()));
	}
}
