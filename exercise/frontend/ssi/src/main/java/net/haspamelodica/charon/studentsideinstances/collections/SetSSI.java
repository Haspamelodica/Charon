package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import java.util.Set;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(Set.class)
@PrototypeClass(SetSSI.Prototype.class)
public interface SetSSI<E extends StudentSideInstance> extends CollectionSSI<E>
{
	public static interface Prototype extends StudentSidePrototype<SetSSI<?>>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> SetSSI<E> of();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> SetSSI<E> of(E e1);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> SetSSI<E> of(@SuppressWarnings("unchecked") E... elements);
	}
}
