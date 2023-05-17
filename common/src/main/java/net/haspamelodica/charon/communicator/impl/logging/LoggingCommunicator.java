package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;

public class LoggingCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, TYPEREF, TC, CM>
{
	private final CommunicationLogger<TYPEREF> logger;

	private final StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator;

	private final TC	loggingTransceiver;
	private final CM	loggingCallbackManager;

	public LoggingCommunicator(CommunicationLoggerParams loggerParams,
			Function<CommunicationLogger<TYPEREF>,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>> createCommunicator,
			BiFunction<CommunicationLogger<TYPEREF>, StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>,
					TC> createLoggingTransceiver,
			BiFunction<CommunicationLogger<TYPEREF>, StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>,
					CM> createLoggingCallbackManager)
	{
		this.logger = new CommunicationLogger<>(loggerParams, this::typerefToTypeName);
		this.communicator = createCommunicator.apply(logger);
		this.loggingTransceiver = createLoggingTransceiver.apply(logger, communicator);
		this.loggingCallbackManager = createLoggingCallbackManager.apply(logger, communicator);
	}

	@Override
	public boolean storeRefsIdentityBased()
	{
		return communicator.storeRefsIdentityBased();
	}

	@Override
	public TYPEREF getTypeByName(String typeName)
	{
		logger.logEnter("type by name " + typeName);
		TYPEREF result = communicator.getTypeByName(typeName);
		logger.logExit(t(result));
		return result;
	}

	@Override
	public TYPEREF getArrayType(TYPEREF componentType)
	{
		logger.logEnter("array type " + t(componentType));
		TYPEREF result = communicator.getArrayType(componentType);
		logger.logExit(t(result));
		return result;
	}

	@Override
	public TYPEREF getTypeOf(REF ref)
	{
		logger.logEnter("type of " + ref);
		TYPEREF result = communicator.getTypeOf(ref);
		logger.logExit(t(result));
		return result;
	}

	@Override
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type)
	{
		logger.logEnter("describe type " + type);
		StudentSideTypeDescription<TYPEREF> result = communicator.describeType(type);
		//TODO maybe format this better?
		logger.logExit(result);
		return result;
	}

	@Override
	public TYPEREF getTypeHandledBySerdes(REF serdesRef)
	{
		logger.logEnter("Serdes type " + serdesRef);
		TYPEREF result = communicator.getTypeHandledBySerdes(serdesRef);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF newArray(TYPEREF componentType, int length)
	{
		logger.logEnter("newarray " + t(componentType) + "[" + length + "]");
		REF result = communicator.newArray(componentType, length);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF newMultiArray(TYPEREF componentType, List<Integer> dimensions)
	{
		logger.logEnter("newarray " + t(componentType) + dimensions.stream().map(i -> i.toString()).collect(Collectors.joining("][", "[", "]")));
		REF result = communicator.newMultiArray(componentType, dimensions);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF newArrayWithInitialValues(TYPEREF componentType, List<REF> initialValues)
	{
		logger.logEnter("newarray " + t(componentType) + " initial " + initialValues.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
		REF result = communicator.newArrayWithInitialValues(componentType, initialValues);
		logger.logExit(result);
		return result;
	}

	@Override
	public int getArrayLength(REF arrayRef)
	{
		logger.logEnter("array length " + arrayRef);
		int result = communicator.getArrayLength(arrayRef);
		logger.logExit(result);
		return result;
	}

	@Override
	public REF getArrayElement(REF arrayRef, int index)
	{
		logger.logEnter("get array " + arrayRef + "[" + index + "]");
		REF result = communicator.getArrayElement(arrayRef, index);
		logger.logExit(result);
		return result;
	}

	@Override
	public void setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		logger.logEnter("set array " + arrayRef + "[" + index + "] = " + valueRef);
		communicator.setArrayElement(arrayRef, index, valueRef);
		logger.logExit();
	}

	@Override
	public RefOrError<REF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs)
	{
		logger.logEnter("new " + t(type) + params.stream().map(this::t).collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		RefOrError<REF> result = communicator.callConstructor(type, params, argRefs);
		logger.logExit(result);
		return result;
	}

	@Override
	public RefOrError<REF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs)
	{
		logger.logEnter(t(returnType) + " " + t(type) + "." + name + params.stream().map(this::t).collect(Collectors.joining(", ", "(", ")")) + ": " + argRefs);
		RefOrError<REF> result = communicator.callStaticMethod(type, name, returnType, params, argRefs);
		logger.logExit(result);
		return result;
	}
	@Override
	public REF getStaticField(TYPEREF type, String name, TYPEREF fieldType)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name);
		REF result = communicator.getStaticField(type, name, fieldType);
		logger.logExit(result);
		return result;
	}
	@Override
	public void setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + " = " + valueRef);
		communicator.setStaticField(type, name, fieldType, valueRef);
		logger.logExit();
	}

	@Override
	public RefOrError<REF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, REF receiverRef, List<REF> argRefs)
	{
		logger.logEnter(t(returnType) + " " + t(type) + "." + name
				+ params.stream().map(this::t).collect(Collectors.joining(", ", "(", ")")) + ": " + receiverRef + ", " + argRefs);
		RefOrError<REF> result = communicator.callInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
		logger.logExit(result);
		return result;
	}
	@Override
	public REF getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + ": " + receiverRef);
		REF result = communicator.getInstanceField(type, name, fieldType, receiverRef);
		logger.logExit(result);
		return result;
	}
	@Override
	public void setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + ": " + receiverRef + " = " + valueRef);
		communicator.setInstanceField(type, name, fieldType, receiverRef, valueRef);
		logger.logExit();
	}

	private String t(TYPEREF typeref)
	{
		return logger.typerefToString(typeref);
	}

	private String typerefToTypeName(TYPEREF typeref)
	{
		return communicator.describeType(typeref).name();
	}

	@Override
	public TC getTransceiver()
	{
		return loggingTransceiver;
	}

	@Override
	public CM getCallbackManager()
	{
		return loggingCallbackManager;
	}
}
