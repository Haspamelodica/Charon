package net.haspamelodica.charon.communicator.impl.reftranslating;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;

public interface RefTranslatorCommunicatorCallbacks<REF_TO> extends StudentSideCommunicatorCallbacks<REF_TO>
{
	public <REF_FROM> REF_TO createForwardRef(UntranslatedRef<REF_FROM> untranslatedRef);
}
