package net.haspamelodica.charon.marshaling;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface Deserializer<T>
{
	public T deserialize(DataInput in) throws IOException;
}
