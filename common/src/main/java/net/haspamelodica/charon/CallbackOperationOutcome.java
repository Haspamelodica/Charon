package net.haspamelodica.charon;

import static net.haspamelodica.charon.CallbackOperationOutcome.Kind.CALLBACK_HIDDEN_ERROR;
import static net.haspamelodica.charon.CallbackOperationOutcome.Kind.CALLBACK_RESULT;
import static net.haspamelodica.charon.CallbackOperationOutcome.Kind.CALLBACK_THROWN;

import java.util.Objects;

import net.haspamelodica.charon.CallbackOperationOutcome.HiddenError;
import net.haspamelodica.charon.CallbackOperationOutcome.Result;
import net.haspamelodica.charon.CallbackOperationOutcome.Thrown;

@SuppressWarnings("rawtypes") // Bug in Eclipse compiler: The types named in permits cannot be parameterized. See https://github.com/eclipse-jdt/eclipse.jdt.core/issues/581
public sealed interface CallbackOperationOutcome<RESULTREF, THROWABLEREF>
		permits Result, Thrown, HiddenError
{
	public Kind kind();

	public static enum Kind
	{
		CALLBACK_RESULT, CALLBACK_THROWN, CALLBACK_HIDDEN_ERROR;

		public byte encode()
		{
			return (byte) ordinal();
		}
		public static Kind decode(byte b)
		{
			return values()[b];
		}
	}

	public static record Result<RESULTREF, THROWABLEREF>(RESULTREF returnValue)
			implements CallbackOperationOutcome<RESULTREF, THROWABLEREF>
	{
		@Override
		public Kind kind()
		{
			return CALLBACK_RESULT;
		}
	}
	public static record Thrown<RESULTREF, THROWABLEREF>(THROWABLEREF thrownThrowable)
			implements CallbackOperationOutcome<RESULTREF, THROWABLEREF>
	{
		public Thrown(THROWABLEREF thrownThrowable)
		{
			this.thrownThrowable = Objects.requireNonNull(thrownThrowable);
		}

		@Override
		public Kind kind()
		{
			return CALLBACK_THROWN;
		}
	}
	public static record HiddenError<RESULTREF, THROWABLEREF>()
			implements CallbackOperationOutcome<RESULTREF, THROWABLEREF>
	{
		@Override
		public Kind kind()
		{
			return CALLBACK_HIDDEN_ERROR;
		}
	}
}
