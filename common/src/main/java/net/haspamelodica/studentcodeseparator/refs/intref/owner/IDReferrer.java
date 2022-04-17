package net.haspamelodica.studentcodeseparator.refs.intref.owner;

public class IDReferrer
{
	private final int	id;
	/** 0 means inactive, a positive value means that number of pending sends, a negative value is illegal state */
	private int			pendingSendsCount;

	/** {@link #pendingSendsCount} starts at 1. */
	public IDReferrer(int id)
	{
		this.id = id;
		this.pendingSendsCount = 1;
	}

	public int id()
	{
		return id;
	}

	/**
	 * If this referrer is <i>deactivated</i>, does nothing and returns <code>false</code>.
	 * Otherwise, increments {@link #pendingSendsCount} by 1 and returns <code>true</code>.
	 * See {@link #decreasePendingSendsCount(int)} for details about deactivation.
	 * 
	 * @return <code>true</code> if this referrer is sill active, otherwise <code>false</code>
	 */
	public boolean incrementPendingSendsCount()
	{
		if(pendingSendsCount == 0)
			return false;

		pendingSendsCount ++;
		return true;
	}
	/**
	 * If this referrer is <i>deactivated</i>, throws an exception.
	 * Otherwise, decreases {@link #pendingSendsCount} by the passed argument,
	 * and if this decrement causes the peding sends count to become 0, deactivates this referrer.
	 * 
	 * @return <code>true</code> if this referrer is deactivated (after decrementing), otherwise <code>false</code>
	 */
	public boolean decreasePendingSendsCount(int receivedCount)
	{
		if(pendingSendsCount == 0)
			//TODO better exception type
			throw new IllegalStateException("Referrer already deactivated.");

		pendingSendsCount -= receivedCount;
		if(pendingSendsCount < 0)
			//TODO better exception type
			throw new IllegalStateException("Less pending sends than received.");

		return pendingSendsCount == 0;
	}
}
