package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind.ObjectKind.CLASS;
import static net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind.PrototypeMethodKind.CONSTRUCTOR;

import net.haspamelodica.studentcodeseparator.annotations.OverrideStudentSideName;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSideObjectMethodKind.ObjectMethodKind;
import net.haspamelodica.studentcodeseparator.annotations.StudentSidePrototypeMethodKind;

@StudentSideObjectKind(CLASS)
@OverrideStudentSideName("net.haspamelodica.studentcodeseparator.MyClassImpl")
public interface MyClass extends StudentSideObject
{
	@StudentSideObjectMethodKind(ObjectMethodKind.INSTANCE_METHOD)
	public void method();

	@StudentSideObjectMethodKind(ObjectMethodKind.INSTANCE_METHOD)
	public void otherMethod(int param);

	@StudentSideObjectMethodKind(ObjectMethodKind.INSTANCE_METHOD)
	public int thirdMethod(String param);

	public static interface Prototype extends StudentSidePrototype<MyClass>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_();

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public MyClass new_(String param);

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
