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

public class LoggingCommunicator<REF,
		THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		TC extends Transceiver, CM extends CallbackManager>
		implements StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
{
	private final CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF> logger;

	private final StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
			? extends TC, ? extends CM> communicator;

	private final TC	loggingTransceiver;
	private final CM	loggingCallbackManager;

	public LoggingCommunicator(CommunicationLoggerParams loggerParams,
			Function<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>,
					StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF,
							? extends TC, ? extends CM>> createCommunicator,
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, StudentSideCommunicator<REF,
					THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>,
					TC> createLoggingTransceiver,
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, StudentSideCommunicator<REF,
					THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>,
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
	public OperationOutcome<TYPEREF, Void, TYPEREF> getTypeByName(String typeName)
	{
		logger.logEnter("type by name " + typeName);
		OperationOutcome<TYPEREF, Void, TYPEREF> result = communicator.getTypeByName(typeName);
		logger.logExit(oV(result));
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
		logger.logEnter("type of " + r(ref));
		TYPEREF result = communicator.getTypeOf(ref);
		logger.logExit(t(result));
		return result;
	}

	@Override
	public StudentSideTypeDescription<TYPEREF> describeType(TYPEREF type)
	{
		logger.logEnter("describe type " + t(type));
		StudentSideTypeDescription<TYPEREF> result = communicator.describeType(type);
		logger.logExit(result.toString(this::t));
		return result;
	}

	@Override
	public TYPEREF getTypeHandledBySerdes(REF serdesRef)
	{
		logger.logEnter("Serdes type " + r(serdesRef));
		TYPEREF result = communicator.getTypeHandledBySerdes(serdesRef);
		logger.logExit(t(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, Void, TYPEREF> createArray(TYPEREF arrayType, int length)
	{
		logger.logEnter("newarray " + t(arrayType) + ": [" + length + "]");
		OperationOutcome<REF, Void, TYPEREF> result = communicator.createArray(arrayType, length);
		logger.logExit(oV(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, Void, TYPEREF> createMultiArray(TYPEREF arrayType, List<Integer> dimensions)
	{
		logger.logEnter("newarray " + t(arrayType) + ": " + dimensions.stream().map(i -> i.toString()).collect(Collectors.joining("][", "[", "]")));
		OperationOutcome<REF, Void, TYPEREF> result = communicator.createMultiArray(arrayType, dimensions);
		logger.logExit(oV(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, Void, TYPEREF> initializeArray(TYPEREF arrayType, List<REF> initialValues)
	{
		logger.logEnter("newarray " + t(arrayType) + " initial " + r(initialValues));
		OperationOutcome<REF, Void, TYPEREF> result = communicator.initializeArray(arrayType, initialValues);
		logger.logExit(oV(result));
		return result;
	}

	@Override
	public int getArrayLength(REF arrayRef)
	{
		logger.logEnter("array length " + r(arrayRef));
		int result = communicator.getArrayLength(arrayRef);
		logger.logExit(result);
		return result;
	}

	@Override
	public OperationOutcome<REF, Void, TYPEREF> getArrayElement(REF arrayRef, int index)
	{
		logger.logEnter("get array " + r(arrayRef) + "[" + index + "]");
		OperationOutcome<REF, Void, TYPEREF> result = communicator.getArrayElement(arrayRef, index);
		logger.logExit(oV(result));
		return result;
	}

	@Override
	public OperationOutcome<Void, Void, TYPEREF> setArrayElement(REF arrayRef, int index, REF valueRef)
	{
		logger.logEnter("set array " + r(arrayRef) + "[" + index + "] = " + r(valueRef));
		OperationOutcome<Void, Void, TYPEREF> result = communicator.setArrayElement(arrayRef, index, valueRef);
		logger.logExit(oVR(result));
		return result;
	}

	@Override
	public OperationOutcome<CONSTRUCTORREF, Void, TYPEREF> lookupConstructor(TYPEREF type, List<TYPEREF> params)
	{
		String constructorString = "new " + t(type) + t(params);
		logger.logEnter("lookup " + constructorString);
		OperationOutcome<CONSTRUCTORREF, Void, TYPEREF> result = communicator.lookupConstructor(type, params);
		logger.registerConstructor(result, constructorString);
		logger.logExit(oV(result));
		return result;
	}
	@Override
	public OperationOutcome<METHODREF, Void, TYPEREF> lookupMethod(TYPEREF type, String name, TYPEREF returnType, List<TYPEREF> params, boolean isStatic)
	{
		String methodString = (isStatic ? "static " : "") + t(returnType) + " " + t(type) + "." + name + t(params);
		logger.logEnter("lookup " + methodString);
		OperationOutcome<METHODREF, Void, TYPEREF> result = communicator.lookupMethod(type, name, returnType, params, isStatic);
		logger.registerMethod(result, methodString);
		logger.logExit(oV(result));
		return result;
	}
	@Override
	public OperationOutcome<FIELDREF, Void, TYPEREF> lookupField(TYPEREF type, String name, TYPEREF fieldType, boolean isStatic)
	{
		String fieldString = (isStatic ? "static " : "") + t(fieldType) + " " + t(type) + "." + name;
		logger.logEnter("lookup " + fieldString);
		OperationOutcome<FIELDREF, Void, TYPEREF> result = communicator.lookupField(type, name, fieldType, isStatic);
		logger.registerField(result, fieldString);
		logger.logExit(oV(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callConstructor(CONSTRUCTORREF constructor, List<REF> argRefs)
	{
		logger.logEnter(c(constructor) + ": " + r(argRefs));
		OperationOutcome<REF, THROWABLEREF, TYPEREF> result = communicator.callConstructor(constructor, argRefs);
		logger.logExit(o(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callStaticMethod(METHODREF method, List<REF> argRefs)
	{
		logger.logEnter(m(method) + ": " + r(argRefs));
		OperationOutcome<REF, THROWABLEREF, TYPEREF> result = communicator.callStaticMethod(method, argRefs);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> getStaticField(FIELDREF field)
	{
		logger.logEnter(f(field));
		OperationOutcome<REF, Void, TYPEREF> result = communicator.getStaticField(field);
		logger.logExit(oV(result));
		return result;
	}
	@Override
	public OperationOutcome<Void, Void, TYPEREF> setStaticField(FIELDREF field, REF valueRef)
	{
		logger.logEnter(f(field) + " = " + r(valueRef));
		OperationOutcome<Void, Void, TYPEREF> result = communicator.setStaticField(field, valueRef);
		logger.logExit(oVR(result));
		return result;
	}

	@Override
	public OperationOutcome<REF, THROWABLEREF, TYPEREF> callInstanceMethod(METHODREF method, REF receiverRef, List<REF> argRefs)
	{
		logger.logEnter(m(method) + ": " + r(receiverRef) + ", " + r(argRefs));
		OperationOutcome<REF, THROWABLEREF, TYPEREF> result = communicator.callInstanceMethod(method, receiverRef, argRefs);
		logger.logExit(o(result));
		return result;
	}
	@Override
	public OperationOutcome<REF, Void, TYPEREF> getInstanceField(FIELDREF field, REF receiverRef)
	{
		logger.logEnter(f(field) + ": " + r(receiverRef));
		OperationOutcome<REF, Void, TYPEREF> result = communicator.getInstanceField(field, receiverRef);
		logger.logExit(oV(result));
		return result;
	}
	@Override
	public OperationOutcome<Void, Void, TYPEREF> setInstanceField(FIELDREF field, REF receiverRef, REF valueRef)
	{
		logger.logEnter(f(field) + ": " + r(receiverRef) + " = " + r(valueRef));
		OperationOutcome<Void, Void, TYPEREF> result = communicator.setInstanceField(field, receiverRef, valueRef);
		logger.logExit(oVR(result));
		return result;
	}

	private String oVR(OperationOutcome<Void, Void, TYPEREF> outcome)
	{
		return logger.outcomeToStringVoidRes(outcome);
	}
	private String oV(OperationOutcome<? extends REF, Void, TYPEREF> outcome)
	{
		return logger.outcomeToStringVoid(outcome);
	}
	private String o(OperationOutcome<? extends REF, ? extends REF, TYPEREF> outcome)
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

	private String c(CONSTRUCTORREF constructor)
	{
		return logger.constructorToString(constructor);
	}

	private String m(METHODREF method)
	{
		return logger.methodToString(method);
	}

	private String f(FIELDREF field)
	{
		return logger.fieldToString(field);
	}

	private String r(List<REF> refs)
	{
		return logger.refsToString(refs);
	}

	private String r(REF ref)
	{
		return logger.refToString(ref);
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
