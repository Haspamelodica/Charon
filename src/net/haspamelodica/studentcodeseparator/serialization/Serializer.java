package net.haspamelodica.studentcodeseparator.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Each subclass of {@link Serializer} has to have a public constructor with no parameters.
 * <br>
 * {@link Serializer#serialize(DataOutputStream, Object)} transmits a snapshot of the object it is passed.
 * Further changes to the original object won't update the object received on the other end. <br>
 * {@link Serializer#deserialize(DataInputStream)} is recommended to, but not required to, return an immutable object
 * to avoid bugs.
 */
public interface Serializer<T>
{
	public Class<T> getHandledClass();
	public void serialize(DataOutputStream out, T obj) throws IOException;
	public T deserialize(DataInputStream in) throws IOException;
}
