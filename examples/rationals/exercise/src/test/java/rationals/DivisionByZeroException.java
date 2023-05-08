package rationals;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;

import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.studentsideinstances.ThrowableSSI;

@StudentSideInstanceKind(CLASS)
public interface DivisionByZeroException extends ThrowableSSI
{
	public static interface Prototype extends StudentSidePrototype<DivisionByZeroException>
	{}
}
