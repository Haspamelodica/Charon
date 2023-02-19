package net.haspamelodica.charon.refs.longref;

public interface LongRefManager<REF>
{
	public REF createManagedRef();
	public REF unmarshalReceivedId(long id);
	public long marshalRefForSending(REF ref);
}
