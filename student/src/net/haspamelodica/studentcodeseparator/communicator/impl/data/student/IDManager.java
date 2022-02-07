package net.haspamelodica.studentcodeseparator.communicator.impl.data.student;

import java.util.Arrays;

import net.haspamelodica.studentcodeseparator.communicator.Ref;

public class IDManager<REF extends Ref<Integer>>
{
	private final Object	lock;
	/** Since 0 represents <code>null</code>, <code>refs[0]</code> is always <code>null</code> */
	private REF[]			refs;
	private int				nextFreeID;

	public IDManager()
	{
		this.lock = new Object();
		@SuppressWarnings("unchecked")
		REF[] refs = (REF[]) new Ref[10];
		this.refs = refs;
		this.nextFreeID = 1;
	}

	public int getID(REF ref)
	{
		if(ref == null)
			return 0;

		// fast path
		Integer idFromAttachment = ref.getAttachment();
		if(idFromAttachment != null)
			return idFromAttachment;

		// slow path
		synchronized(lock)
		{
			idFromAttachment = ref.getAttachment();
			if(idFromAttachment != null)
				return idFromAttachment;

			// Create a new ID
			int id = nextFreeID ++;
			if(id == Integer.MAX_VALUE)
				//TODO better exception type
				throw new RuntimeException("Too many objects");

			// First add to refs array, then set attachment to prevent race conditions.
			// TODO does the JVM guarantee this happens in this order? I don't think so
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

			ref.setAttachment(id);
			return id;
		}
	}

	public REF getRef(int id)
	{
		// It's the server's responsibility that this always succeeds.
		// Also, this handles 0 / null implicitly; see refs
		return refs[id];
	}
}
