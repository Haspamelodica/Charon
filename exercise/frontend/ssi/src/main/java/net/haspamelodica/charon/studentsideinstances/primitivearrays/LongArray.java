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
@StudentSideComponentTypeByClass(long.class)
@PrototypeClass(LongArray.Prototype.class)
public interface LongArray extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(ARRAY_LENGTH)
	public int length();

	@StudentSideInstanceMethodKind(ARRAY_GETTER)
	public long get(int i);

	@StudentSideInstanceMethodKind(ARRAY_SETTER)
	public void set(int i, long value);

	public interface Prototype extends StudentSidePrototype<LongArray>
	{
		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public LongArray create(int length);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public LongArray create(int... dimensions);

		@StudentSidePrototypeMethodKind(ARRAY_CREATOR)
		public LongArray create(List<Integer> dimensions);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public LongArray initialize(long... elements);

		@StudentSidePrototypeMethodKind(Kind.ARRAY_INITIALIZER)
		public LongArray initialize(List<Long> elements);
	}
}
