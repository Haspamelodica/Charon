package net.haspamelodica.charon.communicator.impl.reftranslating;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
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
			public CallbackOperationOutcome<REF_FROM, REF_FROM> callCallbackInstanceMethod(TYPEREF_FROM type, String name,
					TYPEREF_FROM returnType, List<TYPEREF_FROM> params, REF_FROM receiverRef, List<REF_FROM> argRefs)
			{
				return translateFrom(callbacks.callCallbackInstanceMethod(
						translator.translateTo(type), name,
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
	public OperationOutcome<REF_TO, REF_TO> getTypeByName(String typeName)
	{
		return translateTo(communicator.getTypeByName(typeName));
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
	public REF_TO getTypeHandledBySerdes(REF_TO serdesRef)
	{
		return translator.translateTo(communicator.getTypeHandledBySerdes(translator.translateFrom(serdesRef)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> newArray(REF_TO arrayType, int length)
	{
		return translateTo(communicator.newArray(translateTypeFrom(arrayType), length));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> newMultiArray(REF_TO arrayType, List<Integer> dimensions)
	{
		return translateTo(communicator.newMultiArray(translateTypeFrom(arrayType), dimensions));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> newArrayWithInitialValues(REF_TO arrayType, List<REF_TO> initialValues)
	{
		return translateTo(communicator.newArrayWithInitialValues(
				translateTypeFrom(arrayType), translator.translateFrom(initialValues)));
	}
	@Override
	public int getArrayLength(REF_TO arrayRef)
	{
		return communicator.getArrayLength(translator.translateFrom(arrayRef));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> getArrayElement(REF_TO arrayRef, int index)
	{
		return translateTo(communicator.getArrayElement(translator.translateFrom(arrayRef), index));
	}
	@Override
	public OperationOutcome<Void, REF_TO> setArrayElement(REF_TO arrayRef, int index, REF_TO valueRef)
	{
		return translateToVoid(communicator.setArrayElement(translator.translateFrom(arrayRef), index, translator.translateFrom(valueRef)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> callConstructor(REF_TO type, List<REF_TO> params, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callConstructor(translateTypeFrom(type), translateTypeFrom(params),
				translator.translateFrom(argRefs)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> callStaticMethod(REF_TO type, String name, REF_TO returnType, List<REF_TO> params, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callStaticMethod(translateTypeFrom(type), name, translateTypeFrom(returnType),
				translateTypeFrom(params), translator.translateFrom(argRefs)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> getStaticField(REF_TO type, String name, REF_TO fieldType)
	{
		return translateTo(communicator.getStaticField(translateTypeFrom(type), name, translateTypeFrom(fieldType)));
	}
	@Override
	public OperationOutcome<Void, REF_TO> setStaticField(REF_TO type, String name, REF_TO fieldType, REF_TO valueRef)
	{
		return translateToVoid(communicator.setStaticField(translateTypeFrom(type), name, translateTypeFrom(fieldType), translator.translateFrom(valueRef)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> callInstanceMethod(REF_TO type, String name, REF_TO returnType,
			List<REF_TO> params, REF_TO receiverRef, List<REF_TO> argRefs)
	{
		return translateTo(communicator.callInstanceMethod(translateTypeFrom(type), name, translateTypeFrom(returnType),
				translateTypeFrom(params), translator.translateFrom(receiverRef), translator.translateFrom(argRefs)));
	}
	@Override
	public OperationOutcome<REF_TO, REF_TO> getInstanceField(REF_TO type, String name, REF_TO fieldType, REF_TO receiverRef)
	{
		return translateTo(communicator.getInstanceField(translateTypeFrom(type), name, translateTypeFrom(fieldType),
				translator.translateFrom(receiverRef)));
	}
	@Override
	public OperationOutcome<Void, REF_TO> setInstanceField(REF_TO type, String name, REF_TO fieldType, REF_TO receiverRef, REF_TO valueRef)
	{
		return translateToVoid(communicator.setInstanceField(translateTypeFrom(type), name, translateTypeFrom(fieldType),
				translator.translateFrom(receiverRef), translator.translateFrom(valueRef)));
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

	private OperationOutcome<REF_TO, REF_TO> translateTo(OperationOutcome<REF_FROM, TYPEREF_FROM> outcome)
	{
		return translateRefleciveOperationOutcome(outcome, translator::translateTo, translator::translateTo);
	}
	private OperationOutcome<Void, REF_TO> translateToVoid(OperationOutcome<Void, TYPEREF_FROM> outcome)
	{
		return translateRefleciveOperationOutcome(outcome, Function.identity(), translator::translateTo);
	}
	private List<TYPEREF_FROM> translateTypeFrom(List<REF_TO> types)
	{
		return types.stream().map(this::translateTypeFrom).toList();
	}

	@SuppressWarnings("unchecked") // Ensured by caller. Also, since this cast only happens in the student side, it is definitely not a vulnerability.
	private TYPEREF_FROM translateTypeFrom(REF_TO type)
	{
		return (TYPEREF_FROM) translator.translateFrom(type);
	}

	private CallbackOperationOutcome<REF_FROM, REF_FROM> translateFrom(CallbackOperationOutcome<REF_TO, REF_TO> outcome)
	{
		//TODO replace with pattern matching swich once those exist in Java
		return switch(outcome.kind())
		{
			case CALLBACK_RESULT -> new CallbackOperationOutcome.Result<>(
					translator.translateFrom(((CallbackOperationOutcome.Result<REF_TO, REF_TO>) outcome).returnValue()));
			case CALLBACK_THROWN -> new CallbackOperationOutcome.Thrown<>(
					translator.translateFrom(((CallbackOperationOutcome.Thrown<REF_TO, REF_TO>) outcome).thrownThrowable()));
			case CALLBACK_HIDDEN_ERROR -> new CallbackOperationOutcome.HiddenError<>();
		};
	}

	private static <REF_TO, TYPEREF_TO, REF_FROM, TYPEREF_FROM> OperationOutcome<REF_TO, TYPEREF_TO>
			translateRefleciveOperationOutcome(OperationOutcome<REF_FROM, TYPEREF_FROM> outcome,
					Function<REF_FROM, REF_TO> translateRef, Function<TYPEREF_FROM, TYPEREF_TO> translateTyperef)
	{
		Function<List<TYPEREF_FROM>, List<TYPEREF_TO>> translateTyperefs = l -> l.stream().map(translateTyperef).toList();
		//TODO replace with pattern matching swich once those exist in Java
		return switch(outcome.kind())
		{
			case RESULT -> new OperationOutcome.Result<>(
					translateRef.apply(((OperationOutcome.Result<REF_FROM, TYPEREF_FROM>) outcome).returnValue()));
			case SUCCESS_WITHOUT_RESULT -> new OperationOutcome.SuccessWithoutResult<>();
			case THROWN -> new OperationOutcome.Thrown<>(
					translateRef.apply(((OperationOutcome.Thrown<REF_FROM, TYPEREF_FROM>) outcome).thrownThrowable()));
			case CLASS_NOT_FOUND -> new OperationOutcome.ClassNotFound<>(
					((OperationOutcome.ClassNotFound<REF_FROM, TYPEREF_FROM>) outcome).classname());
			case FIELD_NOT_FOUND ->
			{
				OperationOutcome.FieldNotFound<REF_FROM, TYPEREF_FROM> fieldNotFound =
						(OperationOutcome.FieldNotFound<REF_FROM, TYPEREF_FROM>) outcome;
				yield new OperationOutcome.FieldNotFound<>(translateTyperef.apply(fieldNotFound.type()), fieldNotFound.fieldName(),
						translateTyperef.apply(fieldNotFound.fieldType()), fieldNotFound.isStatic());
			}
			case METHOD_NOT_FOUND ->
			{
				OperationOutcome.MethodNotFound<REF_FROM, TYPEREF_FROM> methodNotFound =
						(OperationOutcome.MethodNotFound<REF_FROM, TYPEREF_FROM>) outcome;
				yield new OperationOutcome.MethodNotFound<>(translateTyperef.apply(methodNotFound.type()), methodNotFound.methodName(),
						translateTyperef.apply(methodNotFound.returnType()), translateTyperefs.apply(methodNotFound.parameters()),
						methodNotFound.isStatic());
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				OperationOutcome.ConstructorNotFound<REF_FROM, TYPEREF_FROM> constructorNotFound =
						(OperationOutcome.ConstructorNotFound<REF_FROM, TYPEREF_FROM>) outcome;
				yield new OperationOutcome.ConstructorNotFound<>(translateTyperef.apply(constructorNotFound.type()),
						translateTyperefs.apply(constructorNotFound.parameters()));
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED ->
			{
				OperationOutcome.ConstructorOfAbstractClassCalled<REF_FROM, TYPEREF_FROM> constructorOfAbstractClassCalled =
						(OperationOutcome.ConstructorOfAbstractClassCalled<REF_FROM, TYPEREF_FROM>) outcome;
				yield new OperationOutcome.ConstructorOfAbstractClassCalled<>(translateTyperef.apply(constructorOfAbstractClassCalled.type()),
						translateTyperefs.apply(constructorOfAbstractClassCalled.parameters()));
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				OperationOutcome.ArrayIndexOutOfBounds<REF_FROM, TYPEREF_FROM> arrayIndexOutOfBounds =
						(OperationOutcome.ArrayIndexOutOfBounds<REF_FROM, TYPEREF_FROM>) outcome;
				yield new OperationOutcome.ArrayIndexOutOfBounds<>(arrayIndexOutOfBounds.index(), arrayIndexOutOfBounds.length());
			}
			case ARRAY_SIZE_NEGATIVE -> new OperationOutcome.ArraySizeNegative<>(
					((OperationOutcome.ArraySizeNegative<REF_FROM, TYPEREF_FROM>) outcome).size());
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY -> new OperationOutcome.ArraySizeNegativeInMultiArray<>(
					((OperationOutcome.ArraySizeNegativeInMultiArray<REF_FROM, TYPEREF_FROM>) outcome).dimensions());
		};
	}
}
