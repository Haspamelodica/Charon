package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind.Kind.FIELD_GETTER;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind.Kind.FIELD_SETTER;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_GETTER;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_SETTER;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.UseSerializer;
import net.haspamelodica.studentcodeseparator.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
// In a real exercise, it wouldn't be neccessary to use a different name for student-side instance and implementation
// because both classes never get loaded in the same JVM anyway.
@OverrideStudentSideName("net.haspamelodica.studentcodeseparator.MyClassImpl")
@UseSerializer(StringSerializer.class)
public interface MyClass extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void method();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@OverrideStudentSideName("otherThirdMethod")
	public int thirdMethod(String param);

	@StudentSideInstanceMethodKind(FIELD_GETTER)
	public String myField();

	@StudentSideInstanceMethodKind(FIELD_SETTER)
	public void myField(String value);

	public static interface Prototype extends StudentSidePrototype<MyClass>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_();

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_(String param);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public int staticMethod();

		@StudentSidePrototypeMethodKind(STATIC_FIELD_GETTER)
		public String myStaticField();

		@StudentSidePrototypeMethodKind(STATIC_FIELD_SETTER)
		public void myStaticField(String value);

		public static void test()
		{}
		public default String test2()
		{
			System.out.println("test2");
			return test3();
		}
		private String test3()
		{
			return "hallo";
		}
	}
}
