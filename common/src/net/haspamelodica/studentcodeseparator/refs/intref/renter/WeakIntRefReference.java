package net.haspamelodica.studentcodeseparator.refs.intref.renter;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import net.haspamelodica.studentcodeseparator.refs.Ref;

public class WeakIntRefReference<REF extends Ref<Integer, ?, ?, ?, ?, ?>> extends WeakReference<REF>
{
	private final int			id;
	private final AtomicInteger	receivedCount;

	/** {@link #receivedCount} starts at 1. */
	public WeakIntRefReference(REF referent, ReferenceQueue<REF> queue)
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
