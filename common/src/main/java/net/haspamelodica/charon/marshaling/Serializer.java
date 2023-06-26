package net.haspamelodica.charon.marshaling;

import java.io.DataOutput;
import java.io.IOException;

@FunctionalInterface
public interface Serializer<T>
{
	public void serialize(DataOutput out, T t) throws IOException;
}
