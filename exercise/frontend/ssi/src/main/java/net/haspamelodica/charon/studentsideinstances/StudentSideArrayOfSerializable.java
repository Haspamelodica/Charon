package net.haspamelodica.charon.studentsideinstances;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.marshaling.SerDes;

public interface StudentSideArrayOfSerializable<E> extends StudentSideInstance
{
	public int length();
	public E get(int index);
	public void set(int index, E value);

	public interface Prototype<E, SD extends SerDes<E>, A extends StudentSideArrayOfSerializable<E>> extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E... initialValues);
	}
}
