package net.haspamelodica.charon.refs.direct;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakReferenceWithAttachment<ATTACHMENT, T> extends WeakReference<T>
{
	private final ATTACHMENT attachment;

	public WeakReferenceWithAttachment(T referent, ATTACHMENT attachment, ReferenceQueue<? super T> queue)
	{
		super(referent, queue);
		this.attachment = attachment;
	}

	public ATTACHMENT attachment()
	{
		return attachment;
	}
}
