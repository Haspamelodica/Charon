package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(Iterable.class)
@PrototypeClass(IterableSSI.Prototype.class)
public interface IterableSSI<E extends StudentSideInstance> extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public IteratorSSI<E> iterator();

	public static interface Prototype extends StudentSidePrototype<IterableSSI<?>>
	{}
}
