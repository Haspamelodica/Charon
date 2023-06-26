package net.haspamelodica.charon.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.haspamelodica.charon.StudentSidePrototype;

@Retention(RUNTIME)
@Target(TYPE)
public @interface PrototypeClass
{
	public Class<? extends StudentSidePrototype<?>> value();
}
