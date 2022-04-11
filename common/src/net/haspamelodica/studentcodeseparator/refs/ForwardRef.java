package net.haspamelodica.studentcodeseparator.refs;

public final class ForwardRef<REFT, REFR, FREFT extends REFT, FREFR extends REFR, BREFT extends REFT, BREFR extends REFR>
		implements Ref<REFT, REFR, FREFT, FREFR, BREFT, BREFR>
{
	private final FREFT		referent;
	private volatile FREFR	referrer;

	public ForwardRef(FREFT referent)
	{
		this.referent = referent;
	}

	@Override
	public FREFT referent()
	{
		return referent;
	}
	public void setReferrer(FREFR referrer)
	{
		this.referrer = referrer;
	}
	@Override
	public FREFR referrer()
	{
		return referrer;
	}
}
