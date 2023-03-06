package net.haspamelodica.charon.marshaling;

import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntypedUntranslatedRef;

public interface RepresentationObjectMarshaler<REF>
{
	/** Creates and returns a representation object for the given {@link UntypedUntranslatedRef}. */
	public Object createRepresentationObject(UntranslatedRef<REF> untranslatedRef);
}
