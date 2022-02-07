package net.haspamelodica.studentcodeseparator.communicator.impl.samejvm;

class IdentityObjectContainer
{
	private final Object object;

	public IdentityObjectContainer(Object object)
	{
		this.object = object;
	}

	public Object get()
	{
		return object;
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode(object);
	}
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		IdentityObjectContainer other = (IdentityObjectContainer) obj;
		// yes, '=='. That's the whole point of ObjectContainer.
		return object == other.object;
	}
}
