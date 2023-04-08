package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.List;
import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class RefTranslatorCommunicator<
		REF_TO,
		TC_TO extends Transceiver,
		CM_TO extends CallbackManager,
		REF_FROM,
		TYPEREF_FROM extends REF_FROM,
		TC_FROM extends Transceiver>
		implements StudentSideCommunicator<REF_TO, REF_TO, TC_TO, CM_TO>
{
	private final StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM, ? extends InternalCallbackManager<REF_FROM>>	communicator;
	private final RefTranslator<REF_TO, REF_FROM>																					translator;

	private final boolean storeRefsIdentityBased;

	private final TC_TO	transceiver;
	private final CM_TO	callbackManager;

	public RefTranslatorCommunicator(
			UninitializedStudentSideCommunicator<REF_FROM, TYPEREF_FROM, TC_FROM, InternalCallbackManager<REF_FROM>> communicator,
			boolean storeRefsIdentityBased,
			StudentSideCommunicatorCallbacks<REF_TO, REF_TO> callbacks,
			RefTranslatorCommunicatorCallbacks<REF_TO> refTranslatorCommunicatorCallbacks,
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
					? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, TC_TO> createTransceiver,
			BiFunction<StudentSideCommunicator<REF_FROM, TYPEREF_FROM, ? extends TC_FROM,
					? extends InternalCallbackManager<REF_FROM>>, RefTranslator<REF_TO, REF_FROM>, CM_TO> createCallbackManager)
	{
		this.communicator = communicator.initialize(new StudentSideCommunicatorCallbacks<>()
		{
			@Override
			public RefOrError<REF_FROM> callCallbackInstanceMethod(TYPEREF_FROM type, String name, TYPEREF_FROM returnType, List<TYPEREF_FROM> params,
					REF_FROM receiverRef, List<REF_FROM> argRefs)
			{
				return translator.translateFrom(callbacks.callCallbackInstanceMethod(translator.translateTo(type), name,
						translator.translateTo(returnType),
						translator.translateTo(params),
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
				return refTranslatorCommunicatorCallbacks.createForwardRef(
						new UntranslatedRef<>(RefTranslatorCommunicator.this.communicator, untranslatedRef));
			}

			@Override
			public REF_FROM createBackwardRef(REF_TO translatedRef)
			{
				return RefTranslatorCommunicator.this.communicator.getCallbackManager().createCallbackInstance(callbacks.getCallbackInterfaceCn(translatedRef));
			}
		});

		this.transceiver = createTransceiver.apply(this.communicator, this.translator);
		this.callbackManager = createCallbackManager.apply(this.communicator, this.translator);
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return storeRefsIdentityBased;
	}


	@Override
	public REF_TO getTypeByName(String typeName)
	{
		return translator.translateTo(communicator.getTypeByName(typeName));
	}
	@Override
	public REF_TO getArrayType(REF_TO componentType)
	{
		return translator.translateTo(communicator.getArrayType(translateTypeFrom(componentType)));
	}
	@Override
	public REF_TO getTypeOf(REF_TO ref)
	{
		return translator.translateTo(communicator.getTypeOf(translator.translateFrom(ref)));
	}
	@Override
	public StudentSideTypeDescription<REF_TO> describeType(REF_TO type)
	{
		StudentSideTypeDescription<TYPEREF_FROM> untranslatedResult = communicator.describeType(translateTypeFrom(type));
		return new StudentSideTypeDescription<>(
				untranslatedResult.kind(),
				untranslatedResult.name(),
				untranslatedResult.superclass().map(translator::translateTo),
				untranslatedResult.superinterfaces().stream().map(translator::translateTo).toList(),
				untranslatedResult.componentTypeIfArray().map(translator::translateTo));
	}
	@Override
	public REF_TO newArray(REF_TO componentType, int length)
	{
		return translator.translateTo(communicator.newArray(translateTypeFrom(componentType), length));
	}
	@Override
	public REF_TO newMultiArray(REF_TO componentType, List<Integer> dimensions)
	{
		return translator.translateTo(communicator.newMultiArray(translateTypeFrom(componentType), dimensions));
	}
	@Override
	public int getArrayLength(REF_TO arrayRef)
	{
		return communicator.getArrayLength(translator.translateFrom(arrayRef));
	}
	@Override
	public REF_TO getArrayElement(REF_TO arrayRef, int index)
	{
		return translator.translateTo(communicator.getArrayElement(translator.translateFrom(arrayRef), index));
	}
	@Override
	public void setArrayElement(REF_TO arrayRef, int index, REF_TO valueRef)
	{
		communicator.setArrayElement(translator.translateFrom(arrayRef), index, translator.translateFrom(valueRef));
	}
	@Override
	public RefOrError<REF_TO> callConstructor(REF_TO type, List<REF_TO> params, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callConstructor(translateTypeFrom(type), translateTypeFrom(params),
				translator.translateFrom(argRefs)));
	}
	@Override
	public RefOrError<REF_TO> callStaticMethod(REF_TO type, String name, REF_TO returnType, List<REF_TO> params, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callStaticMethod(translateTypeFrom(type), name, translateTypeFrom(returnType),
				translateTypeFrom(params), translator.translateFrom(argRefs)));
	}
	@Override
	public REF_TO getStaticField(REF_TO type, String name, REF_TO fieldType)
	{
		return translator.translateTo(communicator.getStaticField(translateTypeFrom(type), name, translateTypeFrom(fieldType)));
	}
	@Override
	public void setStaticField(REF_TO type, String name, REF_TO fieldType, REF_TO valueRef)
	{
		communicator.setStaticField(translateTypeFrom(type), name, translateTypeFrom(fieldType), translator.translateFrom(valueRef));
	}
	@Override
	public RefOrError<REF_TO> callInstanceMethod(REF_TO type, String name, REF_TO returnType,
			List<REF_TO> params, REF_TO receiverRef, List<REF_TO> argRefs)
	{
		return translator.translateTo(communicator.callInstanceMethod(translateTypeFrom(type), name, translateTypeFrom(returnType),
				translateTypeFrom(params), translator.translateFrom(receiverRef), translator.translateFrom(argRefs)));
	}
	@Override
	public REF_TO getInstanceField(REF_TO type, String name, REF_TO fieldType, REF_TO receiverRef)
	{
		return translator.translateTo(communicator.getInstanceField(translateTypeFrom(type), name, translateTypeFrom(fieldType),
				translator.translateFrom(receiverRef)));
	}
	@Override
	public void setInstanceField(REF_TO type, String name, REF_TO fieldType, REF_TO receiverRef, REF_TO valueRef)
	{
		communicator.setInstanceField(translateTypeFrom(type), name, translateTypeFrom(fieldType),
				translator.translateFrom(receiverRef), translator.translateFrom(valueRef));
	}

	@Override
	public TC_TO getTransceiver()
	{
		return transceiver;
	}

	@Override
	public CM_TO getCallbackManager()
	{
		return callbackManager;
	}

	private List<TYPEREF_FROM> translateTypeFrom(List<REF_TO> types)
	{
		return types.stream().map(this::translateTypeFrom).toList();
	}

	@SuppressWarnings("unchecked") // ensured by caller
	private TYPEREF_FROM translateTypeFrom(REF_TO type)
	{
		return (TYPEREF_FROM) translator.translateFrom(type);
	}
}
