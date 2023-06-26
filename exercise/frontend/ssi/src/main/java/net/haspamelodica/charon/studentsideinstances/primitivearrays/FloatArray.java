package net.haspamelodica.charon.studentsideinstances.primitivearrays;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.ARRAY;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_LENGTH;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.ARRAY_SETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.ARRAY_CREATOR;

import java.util.List;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideComponentTypeByClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind;

@StudentSideInstanceKind(ARRAY)
@StudentSideComponentTypeByClass(float.class)
@PrototypeClass(FloatArray.Prototype.class)
public interface FloatArray extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(ARRAY_LENGTH)
	public int length();

	@StudentSideInstanceMethodKind(ARRAY_GETTER)
	public float get(int i);

	@StudentSideInstanceMethodKind(ARRAY_SETTER)
	public void set(int i, float value);

	public interface Prototype extends StudentSidePrototype<FloatArray>
	{
		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public FloatArray create(int length);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public FloatArray create(int... dimensions);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public FloatArray create(List<Integer> dimensions);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public FloatArray initialize(float... elements);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public FloatArray initialize(List<Float> elements);
	}
}
