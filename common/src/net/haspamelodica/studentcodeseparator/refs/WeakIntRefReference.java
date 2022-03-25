package net.haspamelodica.studentcodeseparator.refs;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

public class WeakIntRefReference<REFERRER> extends WeakReference<Ref<Integer, REFERRER>>
{
	private final int			id;
	private final AtomicInteger	receivedCount;

	/** {@link #receivedCount} starts at 1. */
	public WeakIntRefReference(Ref<Integer, REFERRER> referent, ReferenceQueue<Ref<Integer, REFERRER>> queue)
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
