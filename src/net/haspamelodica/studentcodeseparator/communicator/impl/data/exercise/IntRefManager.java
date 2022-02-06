package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;

public class IntRefManager
{
	private final Object			lock;
	/**
	 * We need to guarantee only one Ref exists for every student-side object, for comparing SSIs with '=='.
	 * But once no SSI exists anymore (tester-side) referencing a student-side object, that object can be deleted.
	 * (If we didn't do this optimization, student-side object would live forever as the student side would have to keep them reachable
	 * as the tester might at any time use any student-side object.)
	 * (TODO actually do this. {@link ReferenceQueue} seems useful.)
	 * (TODO there might be a race condition: a student-side thread sends a RefID, say, 42;
	 * at the same time the exercise side GC deletes the corresponding IntRef, so the student side gets sent the command to delete ID 42.
	 * The exercise side now receives ID 42 as a "new" object, which wouldn't be a problem by itself,
	 * but the student side will choose a new ID for future uses of that object now.
	 * We could solve this by sending the number of times a certain RefID was received alongside the command to delete an ID
	 * and letting the student side only really delete the object-ID association if it was sent the same number of times.
	 * This won't keep the exercise-side IntRef from being recreated, which isn't a problem,
	 * but will keep two IDs from existing for the same object.)
	 * So, we only keep WeakReferences to created IntRefs:
	 * As long as there is some (strongly) reachable object referencing that IntRef (this includes the corresponding SSI, if any),
	 * that IntRef will be as well, which means the WeakReference won't be cleared.
	 * As soon as an IntRef isn't weakly reachable anymore (implying the corresponding SSI isn't either)
	 * we can throw it away and notify the student side.
	 * <p>
	 * If the same object is made reachable again after the WeakReference to it has been cleared, a new IntRef will be created.
	 * The contract of {@link StudentSideCommunicator} requires that "<code>refA == refB</code> has to be <code>true</code>
	 * iff <code>refA</code> and <code>refB</code> refer to the same student-side object".
	 * However, an IntRef is only recreated if the WeakReference to the old IntRef is cleared,
	 * and neither <code>refA</code> or <code>refB</code> can refer to an IntRef for which WeakReferences have been cleared
	 * as long as {@link IntRef} does not have a finalizer making it reachable again.
	 */
	private WeakReference<IntRef>[]	refs;
	private int						allocatedRefs;

	public IntRefManager()
	{
		this.lock = new Object();
		@SuppressWarnings("unchecked")
		WeakReference<IntRef>[] refs = new WeakReference[10];
		this.refs = refs;
		this.allocatedRefs = 0;
	}

	public void writeRef(DataOutput out, IntRef objRef) throws IOException
	{
		out.writeInt(objRef != null ? objRef.ref() : 0);
	}

	public IntRef readRef(DataInput in, DataOutput out) throws IOException
	{
		int refID = in.readInt();
		if(refID == 0)
			return null;

		// fast path
		if(refID < allocatedRefs)
		{
			WeakReference<IntRef> weakRef = refs[refID];
			if(weakRef != null)
			{
				IntRef ref = weakRef.get();
				if(ref != null)
					return ref;
				// We mustn't change refs here: we aren't synchronized.
				// Do this in the slow path instead.
			}
		}

		//slow path
		synchronized(lock)
		{
			if(refID < allocatedRefs)
			{
				WeakReference<IntRef> weakRef = refs[refID];
				if(weakRef != null)
				{
					IntRef ref = weakRef.get();
					if(ref != null)
						return ref;
					// No need to explicitly delete the weak reference: we'll have to create a new one anyway.
				}
			} else if(refID >= allocatedRefs)
			{
				//TODO only checking with >= allows students to allocate an array of size up to Integer.MAX_VALUE.
				// We might want to prevent that.
				// However, we can't just compare with '==': Multiple threads may try to return new RefIDs at the same time.
				// If we knew the running thread count, we could maybe compare with '<= allocatedRefs + threadCount'.
				allocatedRefs = refID + 1;
				if(allocatedRefs > refs.length)
				{
					int newLength = allocatedRefs * 2;
					// Overflow handling: We do '*2'. This is equivalent to a lshift by one bit.
					// So, we can simply check the sign bit => compare '<0'.
					if(newLength < 0)
						newLength = Integer.MAX_VALUE;
					refs = Arrays.copyOf(refs, newLength);
				}
			}
			// Here we know the ID is new, allocatedRefs is high enough, and the array is big enough.
			IntRef ref = new IntRef(refID);
			refs[refID] = new WeakReference<>(ref);
			return ref;
		}
	}
}
