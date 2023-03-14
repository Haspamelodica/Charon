package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.List;

import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class RefTranslatorCommunicator<REF_TO, REF_FROM, COMM extends StudentSideCommunicator<REF_FROM>>
		implements StudentSideCommunicator<REF_TO>
{
	protected final COMM	communicator;
	private final boolean	storeRefsIdentityBased;

	protected final RefTranslator<REF_TO, REF_FROM> translator;

	public RefTranslatorCommunicator(UninitializedStudentSideCommunicator<REF_FROM, COMM> communicator, boolean storeRefsIdentityBased,
			RefTranslatorCommunicatorCallbacks<REF_TO> callbacks)
	{
		this.communicator = communicator.initialize(new StudentSideCommunicatorCallbacks<>()
		{
			@Override
			public REF_FROM callCallbackInstanceMethod(String cn, String name, String returnClassname, List<String> params,
					REF_FROM receiverRef, List<REF_FROM> argRefs)
			{
				return translator.translateFrom(callbacks.callCallbackInstanceMethod(cn, name, returnClassname, params,
						translator.translateTo(receiverRef), translator.translateTo(argRefs)));
			}

			@Override
			public String getCallbackInterfaceCn(REF_FROM ref)
			{
				return callbacks.getCallbackInterfaceCn(translator.translateTo(ref));
			}
		});
		this.storeRefsIdentityBased = storeRefsIdentityBased;

		this.translator = new RefTranslator<>(storeRefsIdentityBased, this.communicator.storeRefsIdentityBased(), new RefTranslatorCallbacks<>()
		{
			@Override
			public REF_TO createForwardRef(REF_FROM untranslatedRef)
			{
				return callbacks.createForwardRef(new UntranslatedRef<>(RefTranslatorCommunicator.this.communicator, untranslatedRef));
			}

			@Override
			public REF_FROM createBackwardRef(REF_TO translatedRef)
			{
				return RefTranslatorCommunicator.this.communicator.createCallbackInstance(callbacks.getCallbackInterfaceCn(translatedRef));
			}
		});
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
	public String getSuperclass(String cn)
	{
		return communicator.getSuperclass(cn);
	}
	@Override
	public List<String> getInterfaces(String cn)
	{
		return communicator.getInterfaces(cn);
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
	public REF_TO createCallbackInstance(String interfaceCn)
	{
		return translator.translateTo(communicator.createCallbackInstance(interfaceCn));
	}
}
