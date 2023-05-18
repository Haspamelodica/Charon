package net.haspamelodica.charon.studentsideinstances;

import java.util.List;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;

// When changing signature of any method, update StudentSidePrototypeBuilder.
public interface StudentSideArrayOfSSI<E extends StudentSideInstance> extends StudentSideInstance
{
	public int length();
	public E get(int index);
	public void set(int index, E value);

	// When changing type signature, update StudentSidePrototypeBuilder
	// Also, when changing enclosing class, change PrototypeVariant.
	public interface Prototype<E extends StudentSideInstance, A extends StudentSideArrayOfSSI<E>> extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E... initialValues);
		public A createArray(List<E> initialValues);
	}
}
