package net.haspamelodica.charon.marshaling;

import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;

public interface RepresentationObjectMarshaler<REF>
{
	/** Creates and returns a representation object for the given {@link UntranslatedRef}. */
	public Object createRepresentationObject(UntranslatedRef<REF> untranslatedRef);
	public String getCallbackInterfaceCn(Object exerciseSideObject);
}