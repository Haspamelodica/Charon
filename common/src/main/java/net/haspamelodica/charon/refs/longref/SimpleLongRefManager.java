package net.haspamelodica.charon.refs.longref;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.haspamelodica.charon.refs.longref.SimpleLongRefManager.LongRef;

// TODO reintroduce keping alive only neccessary refs
public class SimpleLongRefManager implements LongRefManager<LongRef>
{
	private final boolean				managedRefsHaveNegativeIds;
	private final Map<Long, LongRef>	allRefs;
	private final AtomicLong			nextManagedRefId;

	public SimpleLongRefManager(boolean managedRefsHaveNegativeIds)
	{
		this.managedRefsHaveNegativeIds = managedRefsHaveNegativeIds;
		this.allRefs = new ConcurrentHashMap<>();
		this.nextManagedRefId = new AtomicLong();
	}

	@Override
	public LongRef createManagedRef()
	{
		// [de|in]crementAndGet instead of getAnd[De|In]crement because the IDs should start at +1 / -1, not at 0,
		// since 0 means null.
		long id = managedRefsHaveNegativeIds ? nextManagedRefId.decrementAndGet() : nextManagedRefId.incrementAndGet();
		LongRef ref = new LongRef(id);
		allRefs.put(id, ref);
		return ref;
	}

	@Override
	public LongRef unmarshalReceivedId(long id)
	{
		//TODO in computeIfAbsent, check if the received ID should not be a managed one
		return id == 0 ? null : allRefs.computeIfAbsent(id, LongRef::new);
	}

	@Override
	public long marshalRefForSending(LongRef ref)
	{
		return ref == null ? 0 : ref.id();
	}

	// Can't use java.lang.Long for this since we need to detect Refs becoming unreachable,
	// which won't happen when using Longs due to the long cache
	public class LongRef
	{
		private final long id;

		private LongRef(long id)
		{
			this.id = id;
		}

		private long id()
		{
			return id;
		}

		@Override
		public String toString()
		{
			return "#" + id;
		}
	}
}
