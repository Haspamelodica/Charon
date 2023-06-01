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

import java.util.List;
import java.util.Objects;

import net.haspamelodica.charon.OperationOutcome.ArrayIndexOutOfBounds;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegative;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegativeInMultiArray;
import net.haspamelodica.charon.OperationOutcome.ClassNotFound;
import net.haspamelodica.charon.OperationOutcome.ConstructorNotFound;
import net.haspamelodica.charon.OperationOutcome.ConstructorOfAbstractClassCalled;
import net.haspamelodica.charon.OperationOutcome.FieldNotFound;
import net.haspamelodica.charon.OperationOutcome.MethodNotFound;
import net.haspamelodica.charon.OperationOutcome.Result;
import net.haspamelodica.charon.OperationOutcome.SuccessWithoutResult;
import net.haspamelodica.charon.OperationOutcome.Thrown;

// TODO three type arguments: RESULTREF, THROWABLEREF, TYPEREF
public sealed interface OperationOutcome<REF, TYPEREF>
		permits Result<REF, TYPEREF>, SuccessWithoutResult<REF, TYPEREF>, Thrown<REF, TYPEREF>,
		ClassNotFound<REF, TYPEREF>, FieldNotFound<REF, TYPEREF>, MethodNotFound<REF, TYPEREF>,
		ConstructorNotFound<REF, TYPEREF>, ConstructorOfAbstractClassCalled<REF, TYPEREF>,
		ArrayIndexOutOfBounds<REF, TYPEREF>, ArraySizeNegative<REF, TYPEREF>, ArraySizeNegativeInMultiArray<REF, TYPEREF>
{
	public Kind kind();

	public static enum Kind
	{
		RESULT, SUCCESS_WITHOUT_RESULT, THROWN, CLASS_NOT_FOUND, FIELD_NOT_FOUND, METHOD_NOT_FOUND,
		CONSTRUCTOR_NOT_FOUND, CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED,
		ARRAY_INDEX_OUT_OF_BOUNDS, ARRAY_SIZE_NEGATIVE, ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY;

		public byte encode()
		{
			return (byte) ordinal();
		}
		public static Kind decode(byte b)
		{
			return values()[b];
		}
	}

	public static record Result<REF, TYPEREF>(REF returnValue)
			implements OperationOutcome<REF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return RESULT;
		}
	}
	public static record SuccessWithoutResult<REF, TYPEREF>()
			implements OperationOutcome<REF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return SUCCESS_WITHOUT_RESULT;
		}
	}
	public static record Thrown<REF, TYPEREF>(REF thrownThrowable)
			implements OperationOutcome<REF, TYPEREF>
	{
		public Thrown(REF thrownThrowable)
		{
			this.thrownThrowable = Objects.requireNonNull(thrownThrowable);
		}

		@Override
		public Kind kind()
		{
			return THROWN;
		}
	}
	public static record ClassNotFound<REF, TYPEREF>(String classname)
			implements OperationOutcome<REF, TYPEREF>
	{
		public ClassNotFound(String classname)
		{
			this.classname = Objects.requireNonNull(classname);
		}

		@Override
		public Kind kind()
		{
			return CLASS_NOT_FOUND;
		}
	}
	public static record FieldNotFound<REF, TYPEREF>(TYPEREF type, String fieldName, TYPEREF fieldType, boolean isStatic)
			implements OperationOutcome<REF, TYPEREF>
	{
		public FieldNotFound(TYPEREF type, String fieldName, TYPEREF fieldType, boolean isStatic)
		{
			this.type = Objects.requireNonNull(type);
			this.fieldName = Objects.requireNonNull(fieldName);
			this.fieldType = Objects.requireNonNull(fieldType);
			this.isStatic = isStatic;
		}

		@Override
		public Kind kind()
		{
			return FIELD_NOT_FOUND;
		}
	}
	public static record MethodNotFound<REF, TYPEREF>(TYPEREF type, String methodName,
			TYPEREF returnType, List<TYPEREF> parameters, boolean isStatic) implements OperationOutcome<REF, TYPEREF>
	{
		public MethodNotFound(TYPEREF type, String methodName, TYPEREF returnType, List<TYPEREF> parameters, boolean isStatic)
		{
			this.type = Objects.requireNonNull(type);
			this.methodName = Objects.requireNonNull(methodName);
			this.returnType = Objects.requireNonNull(returnType);
			this.parameters = List.copyOf(parameters);
			this.isStatic = isStatic;
		}

		@Override
		public Kind kind()
		{
			return METHOD_NOT_FOUND;
		}
	}
	public static record ConstructorNotFound<REF, TYPEREF>(TYPEREF type, List<TYPEREF> parameters)
			implements OperationOutcome<REF, TYPEREF>
	{
		public ConstructorNotFound(TYPEREF type, List<TYPEREF> parameters)
		{
			this.type = Objects.requireNonNull(type);
			this.parameters = List.copyOf(parameters);
		}

		@Override
		public Kind kind()
		{
			return CONSTRUCTOR_NOT_FOUND;
		}
	}
	public static record ConstructorOfAbstractClassCalled<REF, TYPEREF>(TYPEREF type, List<TYPEREF> parameters)
			implements OperationOutcome<REF, TYPEREF>
	{
		public ConstructorOfAbstractClassCalled(TYPEREF type, List<TYPEREF> parameters)
		{
			this.type = Objects.requireNonNull(type);
			this.parameters = List.copyOf(parameters);
		}

		@Override
		public Kind kind()
		{
			return CONSTRUCTOR_OF_ABSTRACT_CLASS_CALLED;
		}
	}
	public static record ArrayIndexOutOfBounds<REF, TYPEREF>(int index, int length)
			implements OperationOutcome<REF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return ARRAY_INDEX_OUT_OF_BOUNDS;
		}
	}
	public static record ArraySizeNegative<REF, TYPEREF>(int size)
			implements OperationOutcome<REF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return ARRAY_SIZE_NEGATIVE;
		}
	}
	public static record ArraySizeNegativeInMultiArray<REF, TYPEREF>(List<Integer> dimensions)
			implements OperationOutcome<REF, TYPEREF>
	{
		public ArraySizeNegativeInMultiArray(List<Integer> dimensions)
		{
			this.dimensions = List.copyOf(dimensions);
		}

		@Override
		public Kind kind()
		{
			return ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY;
		}
	}
}
