package net.haspamelodica.charon.marshaling;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * {@link #serialize(DataOutput, Object)} transmits a snapshot of the object it is passed.
 * Further changes to the original object won't update the object received on the other end.
 * <p>
 * <b> {@link #deserialize(DataInput)} must always assume read values to be maliciously crafted! </b>
 * Student code can obtain full control over the student side, and by extension all communication between
 * tester and student side.
 * <p>
 * Each subclass of {@link SerDes} has to have a public constructor with no parameters.
 * <p>
 * {@link #deserialize(DataInput)} is recommended to, but not required to, return an immutable object
 * to help catch bugs.
 * <p>
 * A {@link SerDes} must not cause callbacks being called.
 * {@link SerDes}es must not throw exceptions, except IOExceptions caused by the provided DataInput / DataOutputs.
 */
//TODO make SerDeses causing serializations / callbacks / exceptions possible
public interface SerDes<T> extends Serializer<T>, Deserializer<T>
{
	public Class<T> getHandledClass();
}
