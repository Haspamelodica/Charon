package net.haspamelodica.charon.communicator.impl.reftranslating;

public interface RefTranslatorCallbacks<REF_TO, REF_FROM>
{
	public REF_TO createForwardRef(REF_FROM untranslatedRef);
}
