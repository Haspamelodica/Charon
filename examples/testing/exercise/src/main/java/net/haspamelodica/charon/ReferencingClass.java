package net.haspamelodica.charon;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerializer;
import net.haspamelodica.charon.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
@OverrideStudentSideName("net.haspamelodica.charon.ReferencingClassImpl")
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
