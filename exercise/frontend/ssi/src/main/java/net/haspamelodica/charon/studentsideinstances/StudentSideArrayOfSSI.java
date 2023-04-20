package net.haspamelodica.charon.studentsideinstances;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;

public interface StudentSideArrayOfSSI<E extends StudentSideInstance> extends StudentSideInstance
{
	public int length();
	public E get(int index);
	public void set(int index, E value);

	public interface Prototype<E extends StudentSideInstance, A extends StudentSideArrayOfSSI<E>> extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E... initialValues);
	}
}
