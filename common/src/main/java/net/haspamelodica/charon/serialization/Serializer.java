package net.haspamelodica.charon.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * {@link #serialize(DataOutput, Object)} transmits a snapshot of the object it is passed.
 * Further changes to the original object won't update the object received on the other end.
 * <p>
 * <b> {@link #deserialize(DataInput)} must always assume read values to be maliciously crafted! </b>
 * Student code can obtain full control over the student side, and by extension all communication between
 * tester and student side.
 * <p>
 * Each subclass of {@link Serializer} has to have a public constructor with no parameters.
 * <p>
 * {@link #deserialize(DataInput)} is recommended to, but not required to, return an immutable object
 * to avoid bugs.
 */
public interface Serializer<T>
{
	public Class<T> getHandledClass();
	public void serialize(DataOutput out, T obj) throws IOException;
	public T deserialize(DataInput in) throws IOException;
}
