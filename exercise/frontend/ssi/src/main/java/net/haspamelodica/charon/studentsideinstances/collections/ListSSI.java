package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;
import static net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind.Kind.STATIC_METHOD;

import java.util.List;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;
import net.haspamelodica.charon.annotations.StudentSidePrototypeMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(List.class)
@PrototypeClass(ListSSI.Prototype.class)
public interface ListSSI<E extends StudentSideInstance> extends CollectionSSI<E>
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean addAll(int index, CollectionSSI<? extends E> c);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public E get(int index);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public E set(int index, E element);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void add(int index, E element);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public E remove(int index);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int indexOf(StudentSideInstance o);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int lastIndexOf(StudentSideInstance o);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public ListSSI<E> subList(int fromIndex, int toIndex);

	public static interface Prototype extends StudentSidePrototype<ListSSI<?>>
	{
		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> ListSSI<E> of();

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> ListSSI<E> of(E e1);

		@StudentSidePrototypeMethodKind(STATIC_METHOD)
		public <E extends StudentSideInstance> ListSSI<E> of(@SuppressWarnings("unchecked") E... elements);
	}
}
