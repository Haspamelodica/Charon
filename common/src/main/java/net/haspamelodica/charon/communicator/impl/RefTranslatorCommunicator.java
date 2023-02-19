package net.haspamelodica.charon.communicator.impl;

import java.util.List;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.utils.maps.BidirectionalMap;

public class RefTranslatorCommunicator<REF_TO, REF_FROM, COMM extends StudentSideCommunicator<REF_FROM>> implements StudentSideCommunicator<REF_TO>
{
	protected final COMM	communicator;
	private final boolean	storeRefsIdentityBased;

	private final BidirectionalMap<REF_TO, REF_FROM>	forwardRefs;
	private final BidirectionalMap<REF_TO, REF_FROM>	backwardRefs;

	public RefTranslatorCommunicator(COMM communicator, boolean storeRefsIdentityBased)
	{
		this.communicator = communicator;
		this.storeRefsIdentityBased = storeRefsIdentityBased;

		this.forwardRefs = BidirectionalMap.builder()
				// It is the user's responsibility to keep all forward refs alive
				// as long as they are needed.
				.weakKeys()
				.identityKeys(storeRefsIdentityBased)
				.identityValues(communicator.storeRefsIdentityBased())
				.concurrent()
				.build();
		this.backwardRefs = BidirectionalMap.builder()
				// If the student side doesn't need a callback anymore,
				// it should be reclaimed and the exercise side notified.
				.weakValues()
				.identityKeys(storeRefsIdentityBased)
				.identityValues(communicator.storeRefsIdentityBased())
				.concurrent()
				.build();
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return storeRefsIdentityBased;
	}

	@Override
	public String getClassname(REF_TO ref)
	{
		return communicator.getClassname(translateFrom(ref));
	}
	@Override
	public REF_TO callConstructor(String cn, List<String> params, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callConstructor(cn, params, translateFrom(argRefs)));
	}
	@Override
	public REF_TO callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callStaticMethod(cn, name, returnClassname, params, translateFrom(argRefs)));
	}
	@Override
	public REF_TO getStaticField(String cn, String name, String fieldClassname)
	{
		return translateTo(communicator.getStaticField(cn, name, fieldClassname));
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF_TO valueRef)
	{
		communicator.setStaticField(cn, name, fieldClassname, translateFrom(valueRef));
	}
	@Override
	public REF_TO callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF_TO receiverRef, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callInstanceMethod(cn, name, returnClassname, params, translateFrom(receiverRef), translateFrom(argRefs)));
	}
	@Override
	public REF_TO getInstanceField(String cn, String name, String fieldClassname, REF_TO receiverRef)
	{
		return translateTo(communicator.getInstanceField(cn, name, fieldClassname, translateFrom(receiverRef)));
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF_TO receiverRef, REF_TO valueRef)
	{
		communicator.setInstanceField(cn, name, fieldClassname, translateFrom(receiverRef), translateFrom(valueRef));
	}

	@Override
	public REF_TO createCallbackInstance(String interfaceName, Callback<REF_TO> callback)
	{
		return translateTo(communicator.createCallbackInstance(interfaceName, (cn, name, returnClassname, params, receiverRef, argRefs) -> translateFrom(
				callback.callInstanceMethod(cn, name, returnClassname, params, translateTo(receiverRef), translateTo(argRefs)))));
	}

	protected List<REF_FROM> translateFrom(List<REF_TO> refsTo)
	{
		return refsTo.stream().map(this::translateFrom).toList();
	}
	protected List<REF_TO> translateTo(List<REF_FROM> refsFrom)
	{
		return refsFrom.stream().map(this::translateTo).toList();
	}
	protected REF_FROM translateFrom(REF_TO refTo)
	{
		if(refTo == null)
			return null;

		// If the passed object is a forward ref, we are sure to find the Ref in this map:
		// it can't have been cleared since it is apparently still reachable,
		// otherwise it couldn't have been passed to this method.
		REF_FROM refFrom = forwardRefs.getValue(refTo);
		if(refFrom != null)
			return refFrom;

		return backwardRefs.computeValueIfAbsent(refTo, refTo_ ->
		{
			//TODO the callback is new or has been cleared by now; we need to (re)create it
			throw new UnsupportedOperationException("not implemented yet");
		});
	}
	protected REF_TO translateTo(REF_FROM refFrom)
	{
		if(refFrom == null)
			return null;

		// If the passed Ref is a backward ref (a callback), we are sure to find the object in this map:
		// it can't have been cleared (by the student side) since it is apparently still reachable (by the student side),
		// otherwise the student side wouldn't have passed it to this method.
		REF_TO refTo = backwardRefs.getKey(refFrom);
		if(refTo != null)
			return refTo;

		//TODO we need to keep track of the representation object becoming unreachable
		return forwardRefs.computeKeyIfAbsent(refFrom, refFrom_ ->
		{
			throw new UnsupportedOperationException("not implemented yet");
		});
	}
}
