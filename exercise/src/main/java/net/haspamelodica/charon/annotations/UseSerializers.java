package net.haspamelodica.charon.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
/**
 * See {@link UseSerializer}
 */
public @interface UseSerializers
{
	public UseSerializer[] value();
}
