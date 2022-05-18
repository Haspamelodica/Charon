package net.haspamelodica.charon.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.haspamelodica.charon.serialization.SerDes;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Repeatable(UseSerDeses.class)
/**
 * TODO mention == won't work on deserialized objects
 * <p>
 * Used SerDeses are shared between a student-side instance and its prototype:
 * Adding {@link UseSerDes} (or {@link UseSerDeses}) to either makes that {@link SerDes} usable from the other as well.
 */
public @interface UseSerDes
{
	public Class<? extends SerDes<?>> value();
}
