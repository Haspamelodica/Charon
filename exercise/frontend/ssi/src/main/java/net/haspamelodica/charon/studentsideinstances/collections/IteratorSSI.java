package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;

import java.util.Iterator;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(Iterator.class)
@PrototypeClass(IteratorSSI.Prototype.class)
public interface IteratorSSI<E extends StudentSideInstance> extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean hasNext();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public E next();

	public static interface Prototype extends StudentSidePrototype<IteratorSSI<?>>
	{}
}
