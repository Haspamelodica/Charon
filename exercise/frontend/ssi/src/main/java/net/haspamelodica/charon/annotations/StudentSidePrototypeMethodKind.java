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
public @interface StudentSidePrototypeMethodKind
{
	public enum Kind
	{
		CONSTRUCTOR(CLASS),
		STATIC_METHOD(CLASS, INTERFACE),
		STATIC_FIELD_GETTER(CLASS, INTERFACE),
		STATIC_FIELD_SETTER(CLASS, INTERFACE),
		ARRAY_CREATOR(ARRAY),
		ARRAY_INITIALIZER(ARRAY),
		SERIALIZATION_SENDER(CLASS, INTERFACE);

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
