package net.haspamelodica.charon.marshaling;

import net.haspamelodica.charon.refs.Ref;

public interface RepresentationObjectMarshaler<REPR>
{
	public Class<REPR> representationObjectClass();
	/**
	 * Called only if an object is an instance {@code REPR}
	 * to allow for multiple representation object classes without one common superclass / interface.
	 * <p>
	 * The default implementation always returns {@code true}.
	 */
	public default boolean isRepresentationObjectClass(Class<? extends REPR> obj)
	{
		return true;
	}
	/**
	 * Returns the REF for the given representation object.
	 * The referrer of the returned REF should be the passed representation object.
	 */
	public Ref marshal(REPR obj);
	/**
	 * Creates and returns a representation object for the given REF.
	 * This method is only called if the REF does not have a referrer;
	 * after calling, the caller is responsible for setting the referrer to the returned representation object.
	 */
	public REPR unmarshal(Ref objRef);
}
