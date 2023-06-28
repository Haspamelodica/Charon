package net.haspamelodica.charon.communicator.impl.logging;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.CallbackOperationOutcome;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.OperationOutcome.Kind;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public class CommunicationLogger<REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF>
{
	public static final String DEFAULT_PREFIX = "";

	private final CommunicationLoggerParams					params;
	private final Function<TYPEREF, String>					typerefToTypeName;
	private final UnidirectionalMap<CONSTRUCTORREF, String>	constructorsToString;
	private final UnidirectionalMap<METHODREF, String>		methodsToString;
	private final UnidirectionalMap<FIELDREF, String>		fieldsToString;

	private final ThreadLocal<Integer>	threadId;
	private final ThreadLocal<Integer>	nestingDepth;

	public CommunicationLogger(CommunicationLoggerParams params, Function<TYPEREF, String> typerefToTypeName)
	{
		this.params = params;
		this.typerefToTypeName = typerefToTypeName;
		this.constructorsToString = UnidirectionalMap.builder().concurrent().identityMap().weakKeys().build();
		this.methodsToString = UnidirectionalMap.builder().concurrent().identityMap().weakKeys().build();
		this.fieldsToString = UnidirectionalMap.builder().concurrent().identityMap().weakKeys().build();
		this.threadId = ThreadLocal.withInitial(new AtomicInteger()::incrementAndGet);
		this.nestingDepth = ThreadLocal.withInitial(() -> 0);
	}

	public void logEnter(String message)
	{
		log(false, false, message);
	}
	public void logExit()
	{
		log(false, true, null);
	}
	public void logExit(Object result)
	{
		log(false, true, String.valueOf(result));
	}
	public void logEnterCallback(String message)
	{
		log(true, false, message);
	}
	public void logExitCallback()
	{
		log(true, true, null);
	}
	public void logExitCallback(Object result)
	{
		log(true, true, String.valueOf(result));
	}
	public void log(boolean callback, boolean exit, String message)
	{
		int nestingDepth = this.nestingDepth.get();
		if(!exit)
			nestingDepth ++;

		System.err.println(params.prefix() + "T" + threadId.get() + "\t".repeat(nestingDepth) + (callback ? exit ? "=>" : "<-" : exit ? "<=" : "->") +
				(!exit || message != null ? " " + message : ""));

		if(exit)
			nestingDepth --;
		this.nestingDepth.set(nestingDepth);
	}

	public String callbackOutcomeToString(CallbackOperationOutcome<?, ?> outcome)
	{
		//TODO replace with pattern matching swich once those exist in Java
		return switch(outcome.kind())
		{
			case CALLBACK_RESULT -> String.valueOf(((CallbackOperationOutcome.Result<?, ?>) outcome).returnValue());
			case CALLBACK_THROWN -> "threw " + ((CallbackOperationOutcome.Thrown<?, ?>) outcome).thrownThrowable().toString();
			case CALLBACK_HIDDEN_ERROR -> "hidden error";
		};
	}

	public String outcomeToString(OperationOutcome<?, ?, TYPEREF> outcome)
	{
		//TODO replace with pattern matching swich once those exist in Java
		return switch(outcome.kind())
		{
			case RESULT -> String.valueOf(((OperationOutcome.Result<?, ?, TYPEREF>) outcome).returnValue());
			case SUCCESS_WITHOUT_RESULT -> "";
			case THROWN -> "threw " + ((OperationOutcome.Thrown<?, ?, TYPEREF>) outcome).thrownThrowable().toString();
			case CLASS_NOT_FOUND -> "not found: " + ((OperationOutcome.ClassNotFound<?, ?, TYPEREF>) outcome).classname();
			case FIELD_NOT_FOUND ->
			{
				OperationOutcome.FieldNotFound<?, ?, TYPEREF> fieldNotFound =
						(OperationOutcome.FieldNotFound<?, ?, TYPEREF>) outcome;
				yield "not found: "
						+ (fieldNotFound.isStatic() ? "static " : "") + typerefToString(fieldNotFound.fieldType()) + " "
						+ typerefToString(fieldNotFound.type()) + "." + fieldNotFound.fieldName();
			}
			case METHOD_NOT_FOUND ->
			{
				OperationOutcome.MethodNotFound<?, ?, TYPEREF> methodNotFound =
						(OperationOutcome.MethodNotFound<?, ?, TYPEREF>) outcome;
				yield "not found: "
						+ (methodNotFound.isStatic() ? "static " : "") + typerefToString(methodNotFound.returnType()) + " "
						+ typerefToString(methodNotFound.type()) + "." + methodNotFound.methodName() + typerefsToString(methodNotFound.parameters());
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				OperationOutcome.ConstructorNotFound<?, ?, TYPEREF> constructorNotFound =
						(OperationOutcome.ConstructorNotFound<?, ?, TYPEREF>) outcome;
				yield "not found: "
						+ "" + typerefToString(constructorNotFound.type()) + typerefsToString(constructorNotFound.parameters());
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED ->
			{
				OperationOutcome.ConstructorOfAbstractClassCreated<?, ?, TYPEREF> constructorOfAbstractClassCalled =
						(OperationOutcome.ConstructorOfAbstractClassCreated<?, ?, TYPEREF>) outcome;
				yield "abstract constructor: "
						+ typerefToString(constructorOfAbstractClassCalled.type()) + typerefsToString(constructorOfAbstractClassCalled.parameters());
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				OperationOutcome.ArrayIndexOutOfBounds<?, ?, TYPEREF> arrayIndexOutOfBounds =
						(OperationOutcome.ArrayIndexOutOfBounds<?, ?, TYPEREF>) outcome;
				yield "array index out of bounds: index " + arrayIndexOutOfBounds.index() + ", length " + arrayIndexOutOfBounds.length();
			}
			case ARRAY_SIZE_NEGATIVE -> "array size negative: " + ((OperationOutcome.ArraySizeNegative<?, ?, TYPEREF>) outcome).size();
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY -> "array size negative in multi array: "
					+ ((OperationOutcome.ArraySizeNegativeInMultiArray<?, ?, TYPEREF>) outcome).dimensions();
		};
	}

	public String typerefsToString(List<TYPEREF> typerefs)
	{
		return typerefs.stream().map(this::typerefToString).collect(Collectors.joining(", ", "(", ")"));
	}

	public String typerefToString(TYPEREF typeref)
	{
		return typeref != null ? "<T" + typeref + " " + typerefToTypeName(typeref) + ">" : "<null type>";
	}

	private String typerefToTypeName(TYPEREF typeref)
	{
		return typerefToTypeName.apply(typeref);
	}

	public void registerConstructor(OperationOutcome<CONSTRUCTORREF, ?, TYPEREF> result, String constructorString)
	{
		if(result.kind() == Kind.RESULT)
			constructorsToString.put(((OperationOutcome.Result<CONSTRUCTORREF, ?, TYPEREF>) result).returnValue(),
					constructorString);
	}

	public void registerMethod(OperationOutcome<METHODREF, ?, TYPEREF> result, String methodString)
	{
		if(result.kind() == Kind.RESULT)
			methodsToString.put(((OperationOutcome.Result<METHODREF, ?, TYPEREF>) result).returnValue(),
					methodString);
	}

	public void registerField(OperationOutcome<FIELDREF, ?, TYPEREF> result, String fieldString)
	{
		if(result.kind() == Kind.RESULT)
			fieldsToString.put(((OperationOutcome.Result<FIELDREF, ?, TYPEREF>) result).returnValue(),
					fieldString);
	}

	public String constructorToString(CONSTRUCTORREF constructor)
	{
		return constructor != null ? constructorsToString.get(constructor) : "<null constructor>";
	}

	public String methodToString(METHODREF method)
	{
		return method != null ? methodsToString.get(method) : "<null method>";
	}

	public String fieldToString(FIELDREF field)
	{
		return field != null ? fieldsToString.get(field) : "<null field>";
	}
}