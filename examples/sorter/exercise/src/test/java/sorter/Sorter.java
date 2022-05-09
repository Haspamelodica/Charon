package sorter;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(CLASS)
public interface Sorter extends StudentSideInstance
{
	public interface Prototype extends StudentSidePrototype<Sorter>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public StringArrayList sort(StringArrayList list);
	}
}
