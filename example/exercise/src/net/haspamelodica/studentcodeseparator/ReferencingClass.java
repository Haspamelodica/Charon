package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.UseSerializer;
import net.haspamelodica.studentcodeseparator.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
@OverrideStudentSideName("net.haspamelodica.studentcodeseparator.ReferencingClassImpl")
public interface ReferencingClass extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public MyClass getImpl();

	public static interface Prototype extends StudentSidePrototype<ReferencingClass>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public ReferencingClass new_(MyClass myClass);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public MyClass createImpl();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		@UseSerializer(StringSerializer.class)
		public String myClassImplToString(MyClass impl);
	}
}
