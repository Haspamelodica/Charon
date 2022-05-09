package net.haspamelodica.charon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StudentSideInstanceMethodKind
{
	public enum Kind
	{
		INSTANCE_METHOD,
		INSTANCE_FIELD_GETTER,
		INSTANCE_FIELD_SETTER;
	}

	public Kind value();
}
