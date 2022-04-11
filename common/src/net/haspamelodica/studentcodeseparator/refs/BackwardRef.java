package net.haspamelodica.studentcodeseparator.refs;

public final class BackwardRef<REFT, REFR, FREFT extends REFT, FREFR extends REFR, BREFT extends REFT, BREFR extends REFR>
		implements Ref<REFT, REFR, FREFT, FREFR, BREFT, BREFR>
{
	private final BREFT		referent;
	private volatile BREFR	referrer;

	public BackwardRef(BREFT referent)
	{
		this.referent = referent;
	}

	public BREFT referent()
	{
		return referent;
	}
	public void setReferrer(BREFR referrer)
	{
		this.referrer = referrer;
	}
	@Override
	public BREFR referrer()
	{
		return referrer;
	}
}
