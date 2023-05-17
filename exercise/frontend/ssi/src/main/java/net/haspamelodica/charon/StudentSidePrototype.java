package net.haspamelodica.charon;

import java.util.List;

import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

/**
 * Each subinterface of {@link StudentSidePrototype} represents all operations on one student-side type.
 * For example, for classes this includes calling static methods, reading static fields, writing static fields, and calling constructors.
 * Which kind of student-side operation corresponds to each method on the {@link StudentSidePrototype} is specified
 * by annotating the methods with {@link StudentSidePrototypeMethodKind}.
 */
// When changing type signature, update StudentSidePrototypeBuilder
public interface StudentSidePrototype<SI extends StudentSideInstance>
{
	public static final List<Class<? extends StudentSidePrototype<?>>> DEFAULT_PROTOTYPES = List.of(ThrowableSSI.Prototype.class);
}
