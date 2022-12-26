package net.haspamelodica.charon.refs.intref.renter;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import net.haspamelodica.charon.refs.Ref;

public class WeakIntRefReference extends WeakReference<Ref>
{
	private final int			id;
	private final AtomicInteger	receivedCount;

	/** {@link #receivedCount} starts at 1. */
	public WeakIntRefReference(Ref referent, ReferenceQueue<Ref> queue)
	{
		super(referent, queue);
		this.id = referent.referent();
		this.receivedCount = new AtomicInteger(1);
	}

	public int id()
	{
		return id;
	}

	public void incrementReceivedCount()
	{
		receivedCount.incrementAndGet();
	}

	public int receivedCount()
	{
		return receivedCount.get();
	}
}
