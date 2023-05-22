package net.haspamelodica.charon.studentsideinstances.primitivearrays;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.ARRAY;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_LENGTH;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_SETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.ARRAY_CREATOR;

import java.util.List;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideComponentTypeByClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind;

@StudentSideInstanceKind(ARRAY)
@StudentSideComponentTypeByClass(double.class)
public interface DoubleArray extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(ARRAY_LENGTH)
	public int length();

	@StudentSideInstanceMethodKind(ARRAY_GETTER)
	public double get(int i);

	@StudentSideInstanceMethodKind(ARRAY_SETTER)
	public void set(int i, double value);

	public interface Prototype extends StudentSidePrototype<DoubleArray>
	{
		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public DoubleArray create(int length);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public DoubleArray create(int... dimensions);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public DoubleArray create(List<Integer> dimensions);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public DoubleArray initialize(double... elements);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public DoubleArray initialize(List<Double> elements);
	}
}
