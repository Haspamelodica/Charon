package net.haspamelodica.charon.studentsideinstances;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.marshaling.SerDes;

// When changing signature of any method, update StudentSidePrototypeBuilder.
public interface StudentSideArrayOfSerializable<E> extends StudentSideInstance
{
	public int length();
	public E get(int index);
	public void set(int index, E value);

	// When changing type signature, update StudentSidePrototypeBuilder.
	// Also, when changing enclosing class, change PrototypeVariant.
	public interface Prototype<E, SD extends SerDes<E>, A extends StudentSideArrayOfSerializable<E>> extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E... initialValues);
	}
}
