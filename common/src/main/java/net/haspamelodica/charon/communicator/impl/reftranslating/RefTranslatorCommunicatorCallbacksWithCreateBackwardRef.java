package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.function.Function;

public interface RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO>
		extends RefTranslatorCommunicatorCallbacks<REF_TO>
{
	public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF_TO createBackwardRef(
			UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef);

	public static <REF_TO> RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<REF_TO> fromFunctional(
			Function<UntranslatedRef<?, ?>, REF_TO> createForwardRef,
			Function<UntranslatedRef<?, ?>, REF_TO> createBackwardRef)
	{
		return new RefTranslatorCommunicatorCallbacksWithCreateBackwardRef<>()
		{
			@Override
			public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF_TO createForwardRef(
					UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef)
			{
				return createForwardRef.apply(untranslatedRef);
			}

			@Override
			public <REF_FROM, TYPEREF_FROM extends REF_FROM> REF_TO createBackwardRef(
					UntranslatedRef<REF_FROM, TYPEREF_FROM> untranslatedRef)
			{
				return createBackwardRef.apply(untranslatedRef);
			}
		};
	}
}
