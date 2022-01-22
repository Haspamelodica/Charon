package net.haspamelodica.studentcodeseparator;

import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;

/**
 * Each subinterface of {@link StudentSidePrototype} represents all static operations on one class to be written by the student.
 * This includes calling static methods, reading static fields, writing static fields, and calling constructors.
 * The operations are specified by annotating each method of a subinterface with {@link StudentSidePrototypeMethodKind}.
 */
public interface StudentSidePrototype<SO extends StudentSideObject>
{}
