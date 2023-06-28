package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.function.Function;

public interface RefTranslatorCommunicatorCallbacks<REF_TO>
{
	public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF_TO createForwardRef(
			UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef);

	public static <REF_TO> RefTranslatorCommunicatorCallbacks<REF_TO> fromFunctional(
			Function<UntranslatedRef<?, ?>, REF_TO> createForwardRef)
	{
		// Can't be a lambda because createRef is generic
		return createForwardRef::apply;
	}
}
