package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.UseSerializer;
import net.haspamelodica.studentcodeseparator.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
@OverrideStudentSideName("net.haspamelodica.studentcodeseparator.ReferencingClassImpl")
@UseSerializer(StringSerializer.class)
public interface ReferencingClass extends StudentSideInstance
{
	public static interface Prototype extends StudentSidePrototype<ReferencingClass>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public MyClass createImpl();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public String myClassImplToString(MyClass impl);
	}
}
