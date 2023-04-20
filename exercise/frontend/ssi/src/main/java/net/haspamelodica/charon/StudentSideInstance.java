package net.haspamelodica.charon;

import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;

/**
 * Each subinterface of {@link StudentSideInstance} represents all operations on one student-side object.
 * For example, for student-side classes this includes calling instance methods, reading instance fields, and writing instance fields.
 * Which kind of student-side operation corresponds to each method on the {@link StudentSideInstance} is specified
 * by annotating the methods with {@link StudentSideInstanceMethodKind}.
 */
public interface StudentSideInstance
{}
