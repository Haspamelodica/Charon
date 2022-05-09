package net.haspamelodica.charon.refs.intref.renter;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;

import net.haspamelodica.charon.refs.IllegalRefException;
import net.haspamelodica.charon.refs.Ref;

public class IntRefManager<REF extends Ref<Integer, ?>>
{
	/**
	 * We need to guarantee only one Ref exists for every student-side object, for comparing SSIs with '=='.
	 * But once no SSI exists anymore (tester-side) referencing a student-side object, that object can be deleted.
	 * (If we didn't do this optimization, student-side object would live forever as the student side would have to keep them reachable
	 * as the tester might at any time use any student-side object.)
	 * (There is a race condition: a student-side thread sends a RefID, say, 42;
	 * at the same time the exercise side GC deletes the corresponding IntRef, so the student side gets sent the command to delete ID 42.
	 * The exercise side now receives ID 42 as a "new" object, which wouldn't be a problem by itself,
	 * but the student side will choose a new ID for future uses of that object now.
	 * We solve this by sending the number of times a certain RefID was received alongside the command to delete an ID
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
	 * The contract of StudentSideCommunicator requires
	 * that "<code>refA == refB</code> has to be <code>true</code>
	 * iff <code>refA</code> and <code>refB</code> refer to the same student-side object".
	 * However, an IntRef is only recreated if the WeakReference to the old IntRef is cleared,
	 * and neither <code>refA</code> or <code>refB</code> can refer to an IntRef for which WeakReferences have been cleared
	 * as long as {@link IntRef} does not have a finalizer making it reachable again.
	 */
	private WeakIntRefReference<REF>[] refs;

	private final ReferenceQueue<REF> refQueue;

	private int allocatedRefs;

	private final Object lock;

	public IntRefManager()
	{
		@SuppressWarnings("unchecked")
		WeakIntRefReference<REF>[] refs = new WeakIntRefReference[10];
		this.refs = refs;
		this.refQueue = new ReferenceQueue<>();
		this.allocatedRefs = 0;
		this.lock = new Object();
	}

	public int getID(REF objRef)
	{
		synchronized(lock)
		{
			return objRef != null ? objRef.referent() : 0;
		}
	}

	public REF lookupReceivedRef(int refID) throws IllegalRefException
	{
		synchronized(lock)
		{
			if(refID == 0)
				return null;

			if(refID < allocatedRefs)
			{
				WeakIntRefReference<REF> weakRef = refs[refID];
				if(weakRef != null)
				{
					REF ref = weakRef.get();
					if(ref != null)
					{
						weakRef.incrementReceivedCount();
						// Not sure if this is really needed, but this might avoid some race condition
						// where the weakRef is cleared and polled before the lookup count is incremented
						Reference.reachabilityFence(ref);
						return ref;
					}
					// No need to explicitly delete the weak reference: we'll have to create a new one anyway.
				}
			} else if(refID >= allocatedRefs)
				growRefsToFitID(refID);
			// Here we know the ID is new, allocatedRefs is high enough, and the array is big enough.
			//TODO to fix this unchecked cast, we either have to replace all uses of REF in all classes with Ref<concrete type arguments...>
			// or pass Ref constructors down the entire hierarchy. Same in WeakDirectRefManager.
			REF ref = (REF) new Ref<>(refID);
			refs[refID] = new WeakIntRefReference<>(ref, refQueue);
			// No need to explicitly increment received count: it starts at 1.

			// This is probably very paranoid, but the weakRef _might_ get cleared and polled while it is being constructed,
			// so before the constructor set lookupCount to 1.
			Reference.reachabilityFence(ref);
			return ref;
		}
	}

	private void growRefsToFitID(int refID) throws IllegalRefException
	{
		//TODO The current implementation allows students to allocate an array of size up to Integer.MAX_VALUE.
		// We might want to prevent that.
		// However, we can't just compare with '==': Multiple threads may try to return new RefIDs at the same time.
		// If we knew the running thread count, we could maybe compare with '<= allocatedRefs + threadCount'.

		// The addition below this if might overflow. This results in the student being able to force creation of multiple IntRefs
		// for the same ID. While this doesn't seem very bad, we still prevent it because it might lead to unexpected side effects.
		if(refID == Integer.MAX_VALUE)
			throw new IllegalRefException("Student returned maximal integer as ID: " + Integer.MAX_VALUE);
		allocatedRefs = refID + 1;
		if(allocatedRefs > refs.length)
		{
			int newLength = allocatedRefs * 2;
			// Overflow handling: We do '*2'. This is equivalent to a lshift by one bit.
			// Also, we know allocatedRefs to be positive => sign bit is 0.
			// So, we can simply check the sign bit => compare '<0'.
			if(newLength < 0)
				// Allow such arrays, don't throw an IllegalBehaviourException: The exercise side caused the overflow.
				newLength = Integer.MAX_VALUE;
			refs = Arrays.copyOf(refs, newLength);
		}
	}

	public static record DeletedRef(int id, int receivedCount)
	{}
	/**
	 * Interruptibly waits until an {@link IntRef} returned by this manager gets unreachable and garbage-collected,
	 * then returns an object contiaining the old ref's ID together with how often the ref was looked up
	 * using the {@link #lookupReceivedRef(int)} method.
	 */
	public DeletedRef removeDeletedRef() throws InterruptedException
	{
		@SuppressWarnings("unchecked") // we only put WeakIntRefReferences into the queue
		WeakIntRefReference<REF> ref = (WeakIntRefReference<REF>) refQueue.remove();
		synchronized(lock)
		{
			refs[ref.id()] = null;
		}
		// Once a WeakIntRefReference is in the refQueue, its lookupCount won't be modified anymore.
		// We guarantee this using reachability fences.
		// This means we can safely read lookupCount here without further synchronization or worrying it might change later.
		return new DeletedRef(ref.id(), ref.receivedCount());
	}
}
