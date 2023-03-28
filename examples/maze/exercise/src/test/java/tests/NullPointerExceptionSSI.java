package tests;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;

import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

@StudentSideInstanceKind(CLASS)
@OverrideStudentSideNameByClass(NullPointerException.class)
public interface NullPointerExceptionSSI extends ThrowableSSI
{
	public static interface Prototype extends StudentSidePrototype<NullPointerExceptionSSI>
	{}
}
