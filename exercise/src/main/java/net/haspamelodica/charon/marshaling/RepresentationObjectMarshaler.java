package net.haspamelodica.charon.marshaling;

public interface RepresentationObjectMarshaler<REF>
{
	/** Creates and returns a representation object for the given Ref. */
	public Object createRepresentationObject(REF objRef);
}
