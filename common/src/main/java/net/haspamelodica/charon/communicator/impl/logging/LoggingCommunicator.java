package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideTypeDescription;
import net.haspamelodica.charon.communicator.Transceiver;

public class LoggingCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, TYPEREF, TC, CM>
{
	private final CommunicationLogger<REF, TYPEREF> logger;

	private final StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> communicator;

	private final TC	loggingTransceiver;
	private final CM	loggingCallbackManager;

	public LoggingCommunicator(CommunicationLoggerParams loggerParams,
			Function<CommunicationLogger<REF, TYPEREF>,
					StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>> createCommunicator,
			BiFunction<CommunicationLogger<REF, TYPEREF>, StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>,
					TC> createLoggingTransceiver,
			BiFunction<CommunicationLogger<REF, TYPEREF>, StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM>,
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
	public OperationOutcome<REF, TYPEREF> getTypeByName(String typeName)
	{
		logger.logEnter("type by name " + typeName);
		OperationOutcome<REF, TYPEREF> result = communicator.getTypeByName(typeName);
		logger.logExit(o(result));
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
	public OperationOutcome<REF, TYPEREF> newArray(TYPEREF arrayType, int length)
	{
		logger.logEnter("newarray " + t(arrayType) + ": [" + length + "]");
		OperationOutcome<REF, TYPEREF> result = communicator.newArray(arrayType, length);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, TYPEREF> newMultiArray(TYPEREF arrayType, List<Integer> dimensions)
	{
		logger.logEnter("newarray " + t(arrayType) + ": " + dimensions.stream().map(i -> i.toString()).collect(Collectors.joining("][", "[", "]")));
		OperationOutcome<REF, TYPEREF> result = communicator.newMultiArray(arrayType, dimensions);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, TYPEREF> newArrayWithInitialValues(TYPEREF arrayType, List<REF> initialValues)
	{
		logger.logEnter("newarray " + t(arrayType) + " initial " + initialValues.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
		OperationOutcome<REF, TYPEREF> result = communicator.newArrayWithInitialValues(arrayType, initialValues);
		logger.logExit(o(result));
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
	public OperationOutcome<REF, TYPEREF> getArrayElement(REF arrayRef, int index)
	{
		logger.logEnter("get array " + arrayRef + "[" + index + "]");
		OperationOutcome<REF, TYPEREF> result = communicator.getArrayElement(arrayRef, index);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		logger.logEnter("set array " + arrayRef + "[" + index + "] = " + valueRef);
		OperationOutcome<Void, TYPEREF> result = communicator.setArrayElement(arrayRef, index, valueRef);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, TYPEREF> callConstructor(TYPEREF type, List<TYPEREF> params, List<REF> argRefs)
	{
		logger.logEnter("new " + t(type) + t(params) + ": " + argRefs);
		OperationOutcome<REF, TYPEREF> result = communicator.callConstructor(type, params, argRefs);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, TYPEREF> callStaticMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, List<REF> argRefs)
	{
		logger.logEnter(t(returnType) + " " + t(type) + "." + name + t(params) + ": " + argRefs);
		OperationOutcome<REF, TYPEREF> result = communicator.callStaticMethod(type, name, returnType, params, argRefs);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<REF, TYPEREF> getStaticField(TYPEREF type, String name, TYPEREF fieldType)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name);
		OperationOutcome<REF, TYPEREF> result = communicator.getStaticField(type, name, fieldType);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<Void, TYPEREF> setStaticField(TYPEREF type, String name, TYPEREF fieldType, REF valueRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + " = " + valueRef);
		OperationOutcome<Void, TYPEREF> result = communicator.setStaticField(type, name, fieldType, valueRef);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, TYPEREF> callInstanceMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params,
			REF receiverRef, List<REF> argRefs)
	{
		logger.logEnter(t(returnType) + " " + t(type) + "." + name + t(params) + ": " + receiverRef + ", " + argRefs);
		OperationOutcome<REF, TYPEREF> result = communicator.callInstanceMethod(type, name, returnType, params, receiverRef, argRefs);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<REF, TYPEREF> getInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + ": " + receiverRef);
		OperationOutcome<REF, TYPEREF> result = communicator.getInstanceField(type, name, fieldType, receiverRef);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<Void, TYPEREF> setInstanceField(TYPEREF type, String name, TYPEREF fieldType, REF receiverRef, REF valueRef)
	{
		logger.logEnter(t(fieldType) + " " + t(type) + "." + name + ": " + receiverRef + " = " + valueRef);
		OperationOutcome<Void, TYPEREF> result = communicator.setInstanceField(type, name, fieldType, receiverRef, valueRef);
		logger.logExit(o(result));
		return result;
	}

	private String o(OperationOutcome<?, TYPEREF> outcome)
	{
		return logger.outcomeToString(outcome);
	}

	private String t(List<TYPEREF> typerefs)
	{
		return logger.typerefsToString(typerefs);
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
