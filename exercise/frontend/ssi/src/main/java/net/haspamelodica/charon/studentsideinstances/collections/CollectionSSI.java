package net.haspamelodica.charon.studentsideinstances.collections;

import static net.haspamelodica.charon.annotations.StudentSideInstanceKind.Kind.INTERFACE;
import static net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind.Kind.INSTANCE_METHOD;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.StudentSidePrototype;
import net.haspamelodica.charon.annotations.OverrideStudentSideNameByClass;
import net.haspamelodica.charon.annotations.PrototypeClass;
import net.haspamelodica.charon.annotations.StudentSideInstanceKind;
import net.haspamelodica.charon.annotations.StudentSideInstanceMethodKind;

@StudentSideInstanceKind(INTERFACE)
@OverrideStudentSideNameByClass(Collection.class)
@PrototypeClass(CollectionSSI.Prototype.class)
public interface CollectionSSI<E extends StudentSideInstance> extends IterableSSI<E>
{
	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public int size();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean isEmpty();

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean contains(StudentSideInstance o);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean add(E e);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean remove(StudentSideInstance o);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean containsAll(CollectionSSI<?> c);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean addAll(CollectionSSI<? extends E> c);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean removeAll(CollectionSSI<?> c);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public boolean retainAll(CollectionSSI<?> c);

	@StudentSideInstanceMethodKind(INSTANCE_METHOD)
	public void clear();

	public default Stream<E> pseudoStream()
	{
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pseudoIterator(), 0), false);
	}
	public default Iterable<E> pseudoIterable()
	{
		return this::pseudoIterator;
	}
	public default Iterator<E> pseudoIterator()
	{
		//TODO maybe do some sanity checks
		return new Iterator<>()
		{
			private final IteratorSSI<E> iterator = iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public E next()
			{
				return iterator.next();
			}
		};
	}

	public static interface Prototype extends StudentSidePrototype<CollectionSSI<?>>
	{}
}
