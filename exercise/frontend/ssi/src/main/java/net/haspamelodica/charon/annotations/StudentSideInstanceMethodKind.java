package net.haspamelodica.charon.annotations;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.ARRAY;
import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.CLASS;
import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StudentSideInstanceMethodKind
{
	public enum Kind
	{
		INSTANCE_METHOD(CLASS, INTERFACE),
		INSTANCE_FIELD_GETTER(CLASS),
		INSTANCE_FIELD_SETTER(CLASS),
		ARRAY_LENGTH(ARRAY),
		ARRAY_GETTER(ARRAY),
		ARRAY_SETTER(ARRAY),
		SERIALIZATION_RECEIVER(CLASS, INTERFACE, ARRAY);

		private final Set<StudentSideInstanceKind.Kind> allowedInstanceKinds;

		private Kind(StudentSideInstanceKind.Kind... allowedInstanceKinds)
		{
			this.allowedInstanceKinds = Set.of(allowedInstanceKinds);
		}

		public Set<StudentSideInstanceKind.Kind> allowedInstanceKinds()
		{
			return allowedInstanceKinds;
		}
	}

	public Kind value();
}
