package net.haspamelodica.charon.mockclasses.classloaders;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor.FieldNameExtractor;
import net.bytebuddy.matcher.ElementMatcher;

public interface DynamicClassTransformer
{
	Builder<?> transform(Builder<?> builder, ElementMatcher<FieldDescription> instanceContextFieldMatcher,
			FieldNameExtractor instanceContextFieldNameExtractor);
}
