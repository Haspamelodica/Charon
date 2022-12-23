package net.haspamelodica.charon.utils.maps;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class DebuggingWeakReference<T> extends WeakReference<T>
{
	public DebuggingWeakReference(T referent)
	{
		super(referent);
	}
	public DebuggingWeakReference(T referent, ReferenceQueue<? super T> q)
	{
		super(referent, q);
	}

	@Override
	public String toString()
	{
		T referent = get();
		return referent == null ? "WeakRef<cleared>" : "WeakRef[" + referent + "]";
	}
}
