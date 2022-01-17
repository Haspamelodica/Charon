package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind.ObjectKind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.PrototypeMethodKind.CONSTRUCTOR;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind.ObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.PrototypeMethodKind;

@StudentSideObjectKind(CLASS)
// In a real exercise, it wouldn't be neccessary to use a different name for student-side object and implementation
// because both classes never get loaded in the same JVM anyway.
@OverrideStudentSideName("net.haspamelodica.studentcodeseparator.MyClassImpl")
public interface MyClass extends StudentSideObject
{
	@StudentSideObjectMethodKind(ObjectMethodKind.INSTANCE_METHOD)
	public void method();

	@StudentSideObjectMethodKind(ObjectMethodKind.INSTANCE_METHOD)
	@OverrideStudentSideName("otherThirdMethod")
	public int thirdMethod(String param);

	@StudentSideObjectMethodKind(ObjectMethodKind.FIELD_GETTER)
	public String myField();

	@StudentSideObjectMethodKind(ObjectMethodKind.FIELD_SETTER)
	public void myField(String value);

	public static interface Prototype extends StudentSidePrototype<MyClass>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_();

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_(String param);

		@StudentSidePrototypeMethodKind(PrototypeMethodKind.STATIC_METHOD)
		public int staticMethod();

		@StudentSidePrototypeMethodKind(PrototypeMethodKind.STATIC_FIELD_GETTER)
		public String myStaticField();

		@StudentSidePrototypeMethodKind(PrototypeMethodKind.STATIC_FIELD_SETTER)
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
