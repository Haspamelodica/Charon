package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.List;

import net.haspamelodica.charon.utils.maps.BidirectionalMap;

public class RefTranslator<REF_TO, REF_FROM>
{
	private final RefTranslatorCallbacks<REF_TO, REF_FROM> callbacks;

	private final BidirectionalMap<REF_TO, REF_FROM>	forwardRefs;
	private final BidirectionalMap<REF_TO, REF_FROM>	backwardRefs;

	public RefTranslator(boolean storeToRefsIdentityBased, boolean storeFromRefsIdentityBased, RefTranslatorCallbacks<REF_TO, REF_FROM> callbacks)
	{
		this.callbacks = callbacks;

		this.forwardRefs = BidirectionalMap.builder()
				// It is the user's responsibility to keep all forward refs alive
				// as long as they are needed.
				.weakKeys()
				.identityKeys(storeToRefsIdentityBased)
				.identityValues(storeFromRefsIdentityBased)
				.concurrent()
				.build();
		this.backwardRefs = BidirectionalMap.builder()
				// If the student side doesn't need a callback anymore,
				// it should be reclaimed and the exercise side notified.
				.weakValues()
				.identityKeys(storeToRefsIdentityBased)
				.identityValues(storeFromRefsIdentityBased)
				.concurrent()
				.build();
	}

	public List<REF_FROM> translateFrom(List<REF_TO> refsTo)
	{
		return refsTo.stream().map(this::translateFrom).toList();
	}
	public List<REF_TO> translateTo(List<? extends REF_FROM> refsFrom)
	{
		return refsFrom.stream().map(this::translateTo).toList();
	}

	public REF_FROM translateFrom(REF_TO refTo)
	{
		if(refTo == null)
			return null;

		// If the passed object is a forward ref, we are sure to find the Ref in this map:
		// it can't have been cleared since it is apparently still reachable,
		// otherwise it couldn't have been passed to this method.
		REF_FROM refFrom = forwardRefs.getValue(refTo);
		if(refFrom != null)
			return refFrom;

		return backwardRefs.computeValueIfAbsent(refTo, callbacks::createBackwardRef);
	}
	public REF_TO translateTo(REF_FROM refFrom)
	{
		if(refFrom == null)
			return null;

		// If the passed Ref is a backward ref (a callback), we are sure to find the object in this map:
		// it can't have been cleared (by the student side) since it is apparently still reachable (by the student side),
		// otherwise the student side wouldn't have passed it to this method.
		REF_TO refTo = backwardRefs.getKey(refFrom);
		if(refTo != null)
			return refTo;

		return forwardRefs.computeKeyIfAbsent(refFrom, callbacks::createForwardRef);
	}

	public void setForwardRefTranslation(REF_FROM refFrom, REF_TO refTo)
	{
		forwardRefs.put(refTo, refFrom);
	}

	public void setBackwardRefTranslation(REF_FROM refFrom, REF_TO refTo)
	{
		backwardRefs.put(refTo, refFrom);
	}
}
