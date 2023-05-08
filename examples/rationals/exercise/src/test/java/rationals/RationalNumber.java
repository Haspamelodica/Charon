package rationals;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_GETTER;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(CLASS)
public interface RationalNumber extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
	public int num();

	@StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
	public int den();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public double approximateAsDouble();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public RationalNumber add(RationalNumber other);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public RationalNumber sub(RationalNumber other);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public RationalNumber mul(RationalNumber other);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public RationalNumber div(RationalNumber other);

	public static interface Prototype extends StudentSidePrototype<RationalNumber>
	{
		@StudentSidePrototypeMethodKind(STATIC_FIELD_GETTER)
		public RationalNumber ZERO();

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public RationalNumber new_(int value);

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public RationalNumber new_(int num, int den);
	}
}
