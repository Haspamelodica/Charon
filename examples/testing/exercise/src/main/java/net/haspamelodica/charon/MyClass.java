package net.haspamelodica.charon;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_FIELD_SETTER;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_GETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_FIELD_SETTER;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import net.haspamelodica.charon.annotations.OverrideStudentSideName;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerializer;
import net.haspamelodica.charon.serialization.StringSerializer;

@StudentSideInstanceKind(CLASS)
// In a real exercise, it wouldn't be neccessary to use a different name for student-side instance and implementation
// because both classes never get loaded in the same JVM anyway.
@OverrideStudentSideName("net.haspamelodica.charon.MyClassImpl")
@UseSerializer(StringSerializer.class)
public interface MyClass extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void method();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@OverrideStudentSideName("otherThirdMethod")
	public int thirdMethod(String param);

	@StudentSideInstanceMethodKind(INSTANCE_FIELD_GETTER)
	public String myField();

	@StudentSideInstanceMethodKind(INSTANCE_FIELD_SETTER)
	public void myField(String value);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void sendMessage(String msg);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public String waitForMessage();

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
			return test3();
		}
		private String test3()
		{
			return "test3";
		}
	}
}
