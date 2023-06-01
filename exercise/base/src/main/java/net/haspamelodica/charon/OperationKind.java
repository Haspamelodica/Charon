package net.haspamelodica.charon;

import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_INDEX_OUT_OF_BOUNDS;
import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_SIZE_NEGATIVE;
import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY;
import static net.haspamelodica.charon.OperationOutcome.Kind.CLASS_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.CONSTRUCTOR_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED;
import static net.haspamelodica.charon.OperationOutcome.Kind.FIELD_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.METHOD_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.RESULT;
import static net.haspamelodica.charon.OperationOutcome.Kind.SUCCESS_WITHOUT_RESULT;
import static net.haspamelodica.charon.OperationOutcome.Kind.THROWN;

import java.util.Set;

import net.haspamelodica.charon.exceptions.IllegalBehaviourException;

public enum OperationKind
{
	GET_TYPE_BY_NAME(RESULT, CLASS_NOT_FOUND),
	CREATE_ARRAY(RESULT, ARRAY_SIZE_NEGATIVE),
	CREATE_MULTI_ARRAY(RESULT, ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY),
	INITIALIZE_ARRAY(RESULT),
	GET_ARRAY_ELEMENT(RESULT, ARRAY_INDEX_OUT_OF_BOUNDS),
	SET_ARRAY_ELEMENT(SUCCESS_WITHOUT_RESULT, ARRAY_INDEX_OUT_OF_BOUNDS),
	CALL_CONSTRUCTOR(RESULT, THROWN, CONSTRUCTOR_NOT_FOUND, CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED),
	CALL_METHOD(RESULT, THROWN, METHOD_NOT_FOUND),
	GET_FIELD(RESULT, FIELD_NOT_FOUND),
	SET_FIELD(SUCCESS_WITHOUT_RESULT, FIELD_NOT_FOUND);

	private final Set<OperationOutcome.Kind> allowedOutcomeKinds;

	private OperationKind(OperationOutcome.Kind... allowedOutcomeKinds)
	{
		this.allowedOutcomeKinds = Set.of(allowedOutcomeKinds);
	}

	public <REF, TYPEREF> OperationOutcome<REF, TYPEREF> checkOutcomeAllowed(OperationOutcome<REF, TYPEREF> outcome) throws IllegalBehaviourException
	{
		if(!isOutcomeAllowed(outcome))
			throw new IllegalBehaviourException("Operation of kind " + this + " doesn't allow an outcome of kind " + outcome.kind());
		return outcome;
	}
	public boolean isOutcomeAllowed(OperationOutcome<?, ?> outcome)
	{
		return isOutcomeKindAllowed(outcome.kind());
	}
	public boolean isOutcomeKindAllowed(OperationOutcome.Kind kind)
	{
		return allowedOutcomeKinds.contains(kind);
	}
	public Set<OperationOutcome.Kind> allowedOutcomeKinds()
	{
		return allowedOutcomeKinds;
	}
}
