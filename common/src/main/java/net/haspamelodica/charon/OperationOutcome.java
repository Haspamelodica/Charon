package net.haspamelodica.charon;

import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_INDEX_OUT_OF_BOUNDS;
import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_SIZE_NEGATIVE;
import static net.haspamelodica.charon.OperationOutcome.Kind.ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY;
import static net.haspamelodica.charon.OperationOutcome.Kind.CLASS_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.CONSTRUCTOR_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED;
import static net.haspamelodica.charon.OperationOutcome.Kind.FIELD_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.METHOD_NOT_FOUND;
import static net.haspamelodica.charon.OperationOutcome.Kind.RESULT;
import static net.haspamelodica.charon.OperationOutcome.Kind.SUCCESS_WITHOUT_RESULT;
import static net.haspamelodica.charon.OperationOutcome.Kind.THROWN;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.haspamelodica.charon.OperationOutcome.ArrayIndexOutOfBounds;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegative;
import net.haspamelodica.charon.OperationOutcome.ArraySizeNegativeInMultiArray;
import net.haspamelodica.charon.OperationOutcome.ClassNotFound;
import net.haspamelodica.charon.OperationOutcome.ConstructorNotFound;
import net.haspamelodica.charon.OperationOutcome.ConstructorOfAbstractClassCreated;
import net.haspamelodica.charon.OperationOutcome.FieldNotFound;
import net.haspamelodica.charon.OperationOutcome.MethodNotFound;
import net.haspamelodica.charon.OperationOutcome.Result;
import net.haspamelodica.charon.OperationOutcome.SuccessWithoutResult;
import net.haspamelodica.charon.OperationOutcome.Thrown;

@SuppressWarnings("rawtypes") // Bug in Eclipse compiler: The types named in permits cannot be parameterized. See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/581
public sealed interface OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
		permits Result, SuccessWithoutResult, Thrown, ClassNotFound, FieldNotFound, MethodNotFound, ConstructorNotFound,
		ConstructorOfAbstractClassCreated, ArrayIndexOutOfBounds, ArraySizeNegative, ArraySizeNegativeInMultiArray
{
	public Kind kind();

	public String toString(
			Function<RESULTREF, String> resultrefToString,
			Function<THROWABLEREF, String> throwablerefToString,
			Function<TYPEREF, String> typerefToString);

	private static <TYPEREF> String typerefsToString(Function<TYPEREF, String> typerefToString, List<TYPEREF> typerefs)
	{
		return typerefs.stream().map(typerefToString).collect(Collectors.joining(", ", "(", ")"));
	}

	public static enum Kind
	{
		RESULT, SUCCESS_WITHOUT_RESULT, THROWN, CLASS_NOT_FOUND, FIELD_NOT_FOUND, METHOD_NOT_FOUND,
		CONSTRUCTOR_NOT_FOUND, CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED,
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

	public static record Result<RESULTREF, THROWABLEREF, TYPEREF>(RESULTREF returnValue)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return RESULT;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return resultrefToString.apply(returnValue());
		}
	}
	public static record SuccessWithoutResult<RESULTREF, THROWABLEREF, TYPEREF>()
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return SUCCESS_WITHOUT_RESULT;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "";
		}
	}
	public static record Thrown<RESULTREF, THROWABLEREF, TYPEREF>(THROWABLEREF thrownThrowable)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		public Thrown(THROWABLEREF thrownThrowable)
		{
			this.thrownThrowable = Objects.requireNonNull(thrownThrowable);
		}

		@Override
		public Kind kind()
		{
			return THROWN;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "threw " + thrownThrowable().toString();
		}
	}
	public static record ClassNotFound<RESULTREF, THROWABLEREF, TYPEREF>(String classname)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
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

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "class not found: " + classname();
		}
	}
	public static record FieldNotFound<RESULTREF, THROWABLEREF, TYPEREF>(TYPEREF type, String fieldName, TYPEREF fieldType, boolean isStatic)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
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

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "field not found: "
					+ (isStatic() ? "static " : "") + typerefToString.apply(fieldType()) + " "
					+ typerefToString.apply(type()) + "." + fieldName();
		}
	}
	public static record MethodNotFound<RESULTREF, THROWABLEREF, TYPEREF>(TYPEREF type, String methodName,
			TYPEREF returnType, List<TYPEREF> parameters, boolean isStatic) implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
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

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "method not found: "
					+ (isStatic() ? "static " : "") + typerefToString.apply(returnType()) + " "
					+ typerefToString.apply(type()) + "." + methodName()
					+ typerefsToString(typerefToString, parameters());
		}
	}
	public static record ConstructorNotFound<RESULTREF, THROWABLEREF, TYPEREF>(TYPEREF type, List<TYPEREF> parameters)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
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

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "constructor not found: "
					+ "" + typerefToString.apply(type())
					+ typerefsToString(typerefToString, parameters());
		}
	}
	public static record ConstructorOfAbstractClassCreated<RESULTREF, THROWABLEREF, TYPEREF>(TYPEREF type, List<TYPEREF> parameters)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		public ConstructorOfAbstractClassCreated(TYPEREF type, List<TYPEREF> parameters)
		{
			this.type = Objects.requireNonNull(type);
			this.parameters = List.copyOf(parameters);
		}

		@Override
		public Kind kind()
		{
			return CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "abstract constructor: "
					+ typerefToString.apply(type())
					+ typerefsToString(typerefToString, parameters());
		}
	}
	public static record ArrayIndexOutOfBounds<RESULTREF, THROWABLEREF, TYPEREF>(int index, int length)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return ARRAY_INDEX_OUT_OF_BOUNDS;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "array index out of bounds: index " + index() + ", length " + length();
		}
	}
	public static record ArraySizeNegative<RESULTREF, THROWABLEREF, TYPEREF>(int size)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
	{
		@Override
		public Kind kind()
		{
			return ARRAY_SIZE_NEGATIVE;
		}

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "array size negative: " + size();
		}
	}
	public static record ArraySizeNegativeInMultiArray<RESULTREF, THROWABLEREF, TYPEREF>(List<Integer> dimensions)
			implements OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF>
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

		@Override
		public String toString(Function<RESULTREF, String> resultrefToString,
				Function<THROWABLEREF, String> throwablerefToString, Function<TYPEREF, String> typerefToString)
		{
			return "array size negative in multi array: " + dimensions();
		}
	}
}
