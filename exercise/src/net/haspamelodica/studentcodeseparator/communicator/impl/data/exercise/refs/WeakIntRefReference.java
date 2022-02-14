package net.haspamelodica.studentcodeseparator.communicator.impl.data.exercise.refs;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

public class WeakIntRefReference<ATTACHMENT> extends WeakReference<IntRef<ATTACHMENT>>
{
	private final int			id;
	private final AtomicInteger	receivedCount;

	/** {@link WeakIntRefReference#receivedCount} starts at 1. */
	public WeakIntRefReference(IntRef<ATTACHMENT> referent, ReferenceQueue<IntRef<ATTACHMENT>> queue)
	{
		super(referent, queue);
		this.id = referent.id();
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
