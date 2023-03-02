package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.List;

import net.haspamelodica.charon.communicator.Callback;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;

public class RefTranslatorCommunicator<REF_TO, REF_FROM, COMM extends StudentSideCommunicator<REF_FROM>>
		implements StudentSideCommunicator<REF_TO>
{
	protected final COMM	communicator;
	private final boolean	storeRefsIdentityBased;

	protected final RefTranslator<REF_TO, REF_FROM> translator;

	public RefTranslatorCommunicator(COMM communicator, boolean storeRefsIdentityBased, RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		this.communicator = communicator;
		this.storeRefsIdentityBased = storeRefsIdentityBased;

		this.translator = new RefTranslator<>(storeRefsIdentityBased, storeRefsIdentityBased,
				r -> callbacks.createForwardRef(new UntranslatedRef(communicator, r)));
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return storeRefsIdentityBased;
	}

	@Override
	public String getClassname(REF_TO ref)
	{
		return communicator.getClassname(translator.translateFrom(ref));
	}
	@Override
	public REF_TO callConstructor(String cn, List<String> params, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callConstructor(cn, params, translator.translateFrom(argRefs)));
	}
	@Override
	public REF_TO callStaticMethod(String cn, String name, String returnClassname, List<String> params, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callStaticMethod(cn, name, returnClassname, params, translator.translateFrom(argRefs)));
	}
	@Override
	public REF_TO getStaticField(String cn, String name, String fieldClassname)
	{
		return translator.translateTo(communicator.getStaticField(cn, name, fieldClassname));
	}
	@Override
	public void setStaticField(String cn, String name, String fieldClassname, REF_TO valueRef)
	{
		communicator.setStaticField(cn, name, fieldClassname, translator.translateFrom(valueRef));
	}
	@Override
	public REF_TO callInstanceMethod(String cn, String name, String returnClassname, List<String> params, REF_TO receiverRef, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callInstanceMethod(cn, name, returnClassname, params,
				translator.translateFrom(receiverRef), translator.translateFrom(argRefs)));
	}
	@Override
	public REF_TO getInstanceField(String cn, String name, String fieldClassname, REF_TO receiverRef)
	{
		return translator.translateTo(communicator.getInstanceField(cn, name, fieldClassname, translator.translateFrom(receiverRef)));
	}
	@Override
	public void setInstanceField(String cn, String name, String fieldClassname, REF_TO receiverRef, REF_TO valueRef)
	{
		communicator.setInstanceField(cn, name, fieldClassname, translator.translateFrom(receiverRef), translator.translateFrom(valueRef));
	}

	@Override
	public REF_TO createCallbackInstance(String interfaceName, Callback<REF_TO> callback)
	{
		return translator.translateTo(communicator.createCallbackInstance(interfaceName,
				(cn, name, returnClassname, params, receiverRef, argRefs) -> translator.translateFrom(
						callback.callInstanceMethod(cn, name, returnClassname, params,
								translator.translateTo(receiverRef), translator.translateTo(argRefs)))));
	}
}
