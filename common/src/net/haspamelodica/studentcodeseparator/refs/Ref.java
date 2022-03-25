package net.haspamelodica.studentcodeseparator.refs;

/**
 * There must not be a REF referring to a null value.
 * If null occurs student-side, the exercise-side REF must also be null.
 * <p>
 * Comparing REFs with <code>==</code> is equivalent to checking for student-side object identity.
 * In other words, if <code>refX</code> refers to student-side object <code>x</code>,
 * then <code>refA == refB</code> is <code>true</code> iff <code>a == b</code>.
 * (This holds even if <code>refA</code> and/or <code>refB</code> are <code>null</code>.)
 * <p>
 * Comparing REFs with {@link #equals(Object)} is equivalent to comparing with <code>==</code>
 * (except if the {@link Ref} <code>equals</code> is being called on is <code>null</code>, of course).
 */
public class Ref<REFERENT, REFERRER>
{
	private final REFERENT		referent;
	private volatile REFERRER	referrer;

	public Ref(REFERENT referent)
	{
		this.referent = referent;
	}

	public final REFERENT referent()
	{
		return referent;
	}
	public final void setReferrer(REFERRER referrer)
	{
		this.referrer = referrer;
	}
	public final REFERRER referrer()
	{
		return referrer;
	}
}
