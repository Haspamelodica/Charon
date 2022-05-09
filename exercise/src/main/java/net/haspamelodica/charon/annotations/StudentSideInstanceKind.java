package net.haspamelodica.charon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StudentSideInstanceKind
{
	public enum Kind
	{
		CLASS,
		INTERFACE;
	}

	Kind value();
}
