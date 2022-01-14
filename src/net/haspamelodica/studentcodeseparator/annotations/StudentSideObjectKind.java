package net.haspamelodica.studentcodeseparator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StudentSideObjectKind
{
	public enum ObjectKind
	{
		CLASS, INTERFACE;
	}

	ObjectKind value();
}
