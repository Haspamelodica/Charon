package net.haspamelodica.charon.studentsideinstances;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.marshaling.SerDes;

// Unfortunately we can't extend StudentSideArrayOfSerializable because the two get variants would have the same signature, but different return types.
// When changing signature of any method, update StudentSidePrototypeBuilder.
public interface StudentSideArrayOfSerializableSSI<E_SSI extends StudentSideInstance, E_SER> extends StudentSideInstance
{
	public int length();
	public E_SSI get(int index);
	public E_SER getAsSerializable(int index);
	public void set(int index, E_SSI value);
	public void set(int index, E_SER value);

	// When changing type signature, update StudentSidePrototypeBuilder
	// Also, when changing enclosing class, change PrototypeVariant.
	public interface Prototype<E_SSI extends StudentSideInstance, E_SER, SD extends SerDes<E_SER>, A extends StudentSideArrayOfSerializableSSI<E_SSI, E_SER>>
			extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E_SSI... initialValues);
		public A createArray(@SuppressWarnings("unchecked") E_SER... initialValues);
	}
}
