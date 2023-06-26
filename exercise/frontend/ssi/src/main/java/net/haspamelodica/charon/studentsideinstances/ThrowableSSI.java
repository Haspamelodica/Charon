package net.haspamelodica.charon.studentsideinstances;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.CONSTRUCTOR;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;
import net.haspamelodica.charon.annotations.UseSerDes;
import net.haspamelodica.charon.marshaling.StringSerDes;

@StudentSideInstanceKind(CLASS)
@OverrideStudentSideNameByClass(Throwable.class)
@PrototypeClass(ThrowableSSI.Prototype.class)
public interface ThrowableSSI extends StudentSideInstance
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@UseSerDes(StringSerDes.class)
	public String getMessage();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	@UseSerDes(StringSerDes.class)
	public String getLocalizedMessage();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public ThrowableSSI getCause();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public ThrowableSSI initCause(ThrowableSSI cause);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void printStackTrace();

	//TODO support printStackTrace(PrintStream) / printStackTrace(PrintWriter)

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public ThrowableSSI fillInStackTrace();

	//TODO support getStackTrace / setStackTrace (depend on arrays)

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void addSuppressed(ThrowableSSI exception);

	//TODO support getSuppressed (depends on arrays)

	public static interface Prototype extends StudentSidePrototype<ThrowableSSI>
	{
		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public ThrowableSSI new_();

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public ThrowableSSI new_(String message);

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public ThrowableSSI new_(String message, ThrowableSSI cause);

		@StudentSidePrototypeMethodKind(CONSTRUCTOR)
		public ThrowableSSI new_(ThrowableSSI cause);
	}
}
