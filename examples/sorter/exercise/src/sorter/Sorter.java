package sorter;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.StudentSidePrototype;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(CLASS)
public interface Sorter extends StudentSideInstance
{
	public interface Prototype extends StudentSidePrototype<Sorter>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public StringArrayList sort(StringArrayList list);
	}
}
