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
@StudentSideComponentTypeByClass(int.class)
public interface IntArray extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(ARRAY_LENGTH)
	public int length();

	@StudentSideInstanceMethodKind(ARRAY_GETTER)
	public int get(int i);

	@StudentSideInstanceMethodKind(ARRAY_SETTER)
	public void set(int i, int value);

	public interface Prototype extends StudentSidePrototype<IntArray>
	{
		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public IntArray create(int length);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public IntArray create(int... dimensions);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public IntArray create(List<Integer> dimensions);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public IntArray initialize(int... elements);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public IntArray initialize(List<Integer> elements);
	}
}
