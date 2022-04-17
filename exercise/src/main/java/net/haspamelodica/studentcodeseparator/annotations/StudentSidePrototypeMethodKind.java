package net.haspamelodica.studentcodeseparator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StudentSidePrototypeMethodKind
{
	public enum Kind
	{
		CONSTRUCTOR,
		STATIC_METHOD,
		STATIC_FIELD_GETTER,
		STATIC_FIELD_SETTER;
	}

	public Kind value();
}
