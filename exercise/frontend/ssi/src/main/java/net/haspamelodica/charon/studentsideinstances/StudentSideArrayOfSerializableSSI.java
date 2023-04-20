package net.haspamelodica.charon.studentsideinstances;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.marshaling.SerDes;

// Unfortunately we can't extend StudentSideArrayOfSerializable because the two get variants would have the same signature, but different return types.
public interface StudentSideArrayOfSerializableSSI<E_SSI extends StudentSideInstance, E_SER> extends StudentSideArrayOfSSI<E_SSI>
{
	public int length();
	public E_SER getAsSerializable(int index);
	public void set(int index, E_SER value);

	public interface Prototype<E_SSI extends StudentSideInstance, E_SER, SD extends SerDes<E_SER>, A extends StudentSideArrayOfSerializableSSI<E_SSI, E_SER>>
			extends StudentSidePrototype<A>
	{
		public A createArray(int length);
		public A createArray(int... dimensions);
		public A createArray(@SuppressWarnings("unchecked") E_SSI... initialValues);
		public A createArray(@SuppressWarnings("unchecked") E_SER... initialValues);
	}
}
