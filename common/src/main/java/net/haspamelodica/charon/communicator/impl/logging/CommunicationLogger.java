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

	public String outcomeToStringVoidRes(OperationOutcome<Void, Void, TYPEREF> outcome)
	{
		return outcomeToString(outcome, o -> "", e -> "");
	}
	public String outcomeToStringVoid(OperationOutcome<? extends REF, Void, TYPEREF> outcome)
	{
		return outcomeToString(outcome, this::refToString, e -> "");
	}
	public String outcomeToString(OperationOutcome<? extends REF, ? extends REF, TYPEREF> outcome)
	{
		return outcomeToString(outcome, this::refToString, this::refToString);
	}

	public <RESULTREF, THROWABLEREF> String outcomeToString(OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF> outcome,
			Function<RESULTREF, String> resultrefToString, Function<THROWABLEREF, String> throwablerefToString)
	{
		//TODO replace with pattern matching swich once those exist in Java
		return switch(outcome.kind())
		{
			case RESULT -> resultrefToString.apply(((OperationOutcome.Result<RESULTREF, THROWABLEREF, TYPEREF>) outcome).returnValue());
			case SUCCESS_WITHOUT_RESULT -> "";
			case THROWN -> "threw " + ((OperationOutcome.Thrown<RESULTREF, THROWABLEREF, TYPEREF>) outcome).thrownThrowable().toString();
			case CLASS_NOT_FOUND -> "not found: " + ((OperationOutcome.ClassNotFound<RESULTREF, THROWABLEREF, TYPEREF>) outcome).classname();
			case FIELD_NOT_FOUND ->
			{
				OperationOutcome.FieldNotFound<RESULTREF, THROWABLEREF, TYPEREF> fieldNotFound =
						(OperationOutcome.FieldNotFound<RESULTREF, THROWABLEREF, TYPEREF>) outcome;
				yield "not found: "
						+ (fieldNotFound.isStatic() ? "static " : "") + typerefToString(fieldNotFound.fieldType()) + " "
						+ typerefToString(fieldNotFound.type()) + "." + fieldNotFound.fieldName();
			}
			case METHOD_NOT_FOUND ->
			{
				OperationOutcome.MethodNotFound<RESULTREF, THROWABLEREF, TYPEREF> methodNotFound =
						(OperationOutcome.MethodNotFound<RESULTREF, THROWABLEREF, TYPEREF>) outcome;
				yield "not found: "
						+ (methodNotFound.isStatic() ? "static " : "") + typerefToString(methodNotFound.returnType()) + " "
						+ typerefToString(methodNotFound.type()) + "." + methodNotFound.methodName() + typerefsToString(methodNotFound.parameters());
			}
			case CONSTRUCTOR_NOT_FOUND ->
			{
				OperationOutcome.ConstructorNotFound<RESULTREF, THROWABLEREF, TYPEREF> constructorNotFound =
						(OperationOutcome.ConstructorNotFound<RESULTREF, THROWABLEREF, TYPEREF>) outcome;
				yield "not found: "
						+ "" + typerefToString(constructorNotFound.type()) + typerefsToString(constructorNotFound.parameters());
			}
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED ->
			{
				OperationOutcome.ConstructorOfAbstractClassCreated<RESULTREF, THROWABLEREF, TYPEREF> constructorOfAbstractClassCalled =
						(OperationOutcome.ConstructorOfAbstractClassCreated<RESULTREF, THROWABLEREF, TYPEREF>) outcome;
				yield "abstract constructor: "
						+ typerefToString(constructorOfAbstractClassCalled.type()) + typerefsToString(constructorOfAbstractClassCalled.parameters());
			}
			case ARRAY_INDEX_OUT_OF_BOUNDS ->
			{
				OperationOutcome.ArrayIndexOutOfBounds<RESULTREF, THROWABLEREF, TYPEREF> arrayIndexOutOfBounds =
						(OperationOutcome.ArrayIndexOutOfBounds<RESULTREF, THROWABLEREF, TYPEREF>) outcome;
				yield "array index out of bounds: index " + arrayIndexOutOfBounds.index() + ", length " + arrayIndexOutOfBounds.length();
			}
			case ARRAY_SIZE_NEGATIVE -> "array size negative: "
					+ ((OperationOutcome.ArraySizeNegative<RESULTREF, THROWABLEREF, TYPEREF>) outcome).size();
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY -> "array size negative in multi array: "
					+ ((OperationOutcome.ArraySizeNegativeInMultiArray<RESULTREF, THROWABLEREF, TYPEREF>) outcome).dimensions();
		};
	}

	public String typerefsToString(List<TYPEREF> typerefs)
	{
		return typerefs.stream().map(this::typerefToString).collect(Collectors.joining(", ", "(", ")"));
	}

	public String typerefToString(TYPEREF typeref)
	{
		return typeref != null ? "<T" + refToString(typeref) + " " + typerefToTypeName(typeref) + ">" : "<null type>";
	}

	private String typerefToTypeName(TYPEREF typeref)
	{
		return typerefToTypeName.apply(typeref);
	}

	public void registerConstructor(OperationOutcome<CONSTRUCTORREF, ?, TYPEREF> result, String constructorString)
	{
		if(result.kind() == Kind.RESULT)
		{
			CONSTRUCTORREF constructorref = ((OperationOutcome.Result<CONSTRUCTORREF, ?, TYPEREF>) result).returnValue();
			constructorsToString.put(constructorref, "<C" + refToString(constructorref) + " " + constructorString + ">");
		}
	}

	public void registerMethod(OperationOutcome<METHODREF, ?, TYPEREF> result, String methodString)
	{
		if(result.kind() == Kind.RESULT)
		{
			METHODREF methodref = ((OperationOutcome.Result<METHODREF, ?, TYPEREF>) result).returnValue();
			methodsToString.put(methodref, "<M" + refToString(methodref) + " " + methodString + ">");
		}
	}

	public void registerField(OperationOutcome<FIELDREF, ?, TYPEREF> result, String fieldString)
	{
		if(result.kind() == Kind.RESULT)
		{
			FIELDREF fieldref = ((OperationOutcome.Result<FIELDREF, ?, TYPEREF>) result).returnValue();
			fieldsToString.put(fieldref, "<F" + refToString(fieldref) + " " + fieldString + ">");
		}
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

	public String refsToString(List<REF> refs)
	{
		return refs.stream().map(this::refToString).collect(Collectors.joining(", ", "(", ")"));
	}

	public String refToString(REF ref)
	{
		return !params.useRefToString() ? "<ref>" : String.valueOf(ref);
	}

	public String objectToString(Object object)
	{
		return !params.useObjectToString() ? "<object>" : String.valueOf(object);
	}
}