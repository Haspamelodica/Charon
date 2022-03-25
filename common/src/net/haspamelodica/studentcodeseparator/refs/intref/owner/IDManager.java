package net.haspamelodica.studentcodeseparator.refs.intref.owner;

import java.util.Arrays;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public class IDManager<REFERENT>
{
	/** Since 0 represents <code>null</code>, <code>refs[0]</code> is always <code>null</code> */
	private Ref<REFERENT, IDReferrer>[]	refs;
	private int							nextFreeID;

	private final Object lock;

	public IDManager()
	{
		@SuppressWarnings("unchecked")
		Ref<REFERENT, IDReferrer>[] refs = (Ref<REFERENT, IDReferrer>[]) new Ref[10];
		this.refs = refs;
		this.nextFreeID = 1;

		this.lock = new Object();
	}

	public int getIDForSending(Ref<REFERENT, IDReferrer> ref)
	{
		synchronized(lock)
		{
			if(ref == null)
				return 0;

			IDReferrer referrer = ref.referrer();
			if(referrer != null)
				if(referrer.incrementPendingSendsCount())
					return referrer.id();

			// Create a new ID. _Don't_ reuse the old one if one exists.
			int id = nextFreeID;
			if(id == Integer.MAX_VALUE)
				//TODO better exception type
				throw new RuntimeException("Too many objects");
			// only increment after checking for MAX_VALUE
			nextFreeID ++;

			if(id >= refs.length)
			{
				int newLength = (id + 1) * 2;
				// Overflow handling: We do '*2'. This is equivalent to a lshift by one bit.
				// Also, we know (id+1) to be positive => sign bit is 0.
				// So, we can simply check the sign bit => compare '<0'.
				if(newLength < 0)
					newLength = Integer.MAX_VALUE;
				refs = Arrays.copyOf(refs, newLength);
			}

			// No need to explicitly increment pending sent count: starts at 1
			ref.setReferrer(new IDReferrer(id));
			refs[id] = ref;

			return id;
		}
	}

	public Ref<REFERENT, IDReferrer> getRef(int id)
	{
		synchronized(lock)
		{
			// It's the server's responsibility that the index is always in range.
			// Also, this handles 0 / null implicitly; see refs.
			return refs[id];
		}
	}

	public void refDeleted(Ref<REFERENT, IDReferrer> deletedRef, int receivedCount)
	{
		synchronized(lock)
		{
			IDReferrer referrer = deletedRef.referrer();
			boolean refNowInactive = referrer.decreasePendingSendsCount(receivedCount);
			if(refNowInactive)
				// delete the reference to the now-unused ref to make it accessible for the GC
				refs[referrer.id()] = null;
		}
	}
}
