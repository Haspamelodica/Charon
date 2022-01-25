package net.haspamelodica.studentcodeseparator.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.haspamelodica.studentcodeseparator.serialization.Serializer;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Repeatable(UseSerializers.class)
/**
 * Used serializers are shared between a student-side instance and its prototype:
 * Adding {@link UseSerializer} (or {@link UseSerializers}) to either makes that serializer usable from the other as well.
 */
public @interface UseSerializer
{
	public Class<? extends Serializer<?>> value();
}
