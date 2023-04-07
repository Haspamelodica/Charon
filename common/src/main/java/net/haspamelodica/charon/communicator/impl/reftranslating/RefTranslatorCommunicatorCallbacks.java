package net.haspamelodica.charon.communicator.impl.reftranslating;

public interface RefTranslatorCommunicatorCallbacks<REF_TO>
{
	public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF_TO createForwardRef(UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef);
}
