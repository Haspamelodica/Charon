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
public sealed interface Ref<REFT, REFR, FREFT extends REFT, FREFR extends REFR, BREFT extends REFT,
		BREFR extends REFR> permits ForwardRef<REFT, REFR, FREFT, FREFR, BREFT, BREFR>,BackwardRef<REFT, REFR, FREFT, FREFR, BREFT, BREFR>
{
	public REFT referent();
	//No setReferrer: we don't know whether it needs FREFR or BREFR
	public REFR referrer();
}
