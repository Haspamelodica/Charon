package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import java.util.Arrays;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class IDManager<REF extends Ref<DataCommunicatorAttachment>>
{
	/** Since 0 represents <code>null</code>, <code>refs[0]</code> is always <code>null</code> */
	private REF[]	refs;
	private int		nextFreeID;

	public IDManager()
	{
		@SuppressWarnings("unchecked")
		REF[] refs = (REF[]) new Ref[10];
		this.refs = refs;
		this.nextFreeID = 1;
	}

	/** Not thread-safe. Callers must make sure only one thread is in {@link #getIDForSending(IntRef)}, {@link #getRef(int)} or {@link #refDeleted(Ref, int)}. */
	public int getIDForSending(REF ref)
	{
		if(ref == null)
			return 0;

		DataCommunicatorAttachment attachment = ref.getAttachment();
		if(attachment != null)
			if(attachment.incrementPendingSendsCount())
				return attachment.id();

		// Create a new ID
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

		refs[id] = ref;

		// No need to explicitly increment pending sent count: starts at 1
		ref.setAttachment(new DataCommunicatorAttachment(id));
		return id;
	}

	/** Not thread-safe. Callers must make sure only one thread is in {@link #getIDForSending(IntRef)}, {@link #getRef(int)} or {@link #refDeleted(Ref, int)}. */
	public REF getRef(int id)
	{
		// It's the server's responsibility that the index is always in range.
		// Also, this handles 0 / null implicitly; see refs.
		return refs[id];
	}

	/** Not thread-safe. Callers must make sure only one thread is in {@link #getIDForSending(IntRef)}, {@link #getRef(int)} or {@link #refDeleted(Ref, int)}. */
	public void refDeleted(REF deletedRef, int receivedCount)
	{
		DataCommunicatorAttachment attachment = deletedRef.getAttachment();
		boolean refNowInactive = attachment.decreasePendingSendsCount(receivedCount);
		if(refNowInactive)
			// delete the reference to the now-unused ref to make it accessible for the GC
			refs[attachment.id()] = null;
	}
}
