package net.haspamelodica.studentcodeseparator.serialization;

import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.castOrPrimitive;
import static net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils.classToName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import net.haspamelodica.studentcodeseparator.StudentSideInstance;
import net.haspamelodica.studentcodeseparator.communicator.Ref;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.SerializationException;
import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;

public class SerializationHandler<ATTACHMENT, REF extends Ref<ATTACHMENT>>
{
	private final StudentSideCommunicator<ATTACHMENT, REF>	communicator;
	private final Function<StudentSideInstance, REF>		refForStudentSideInstance;
	private final Function<REF, StudentSideInstance>		studentSideInstanceForRef;
	private final List<Class<? extends Serializer<?>>>		serializerClasses;

	private final ConcurrentMap<Class<? extends Serializer<?>>, InitializedSerializer<ATTACHMENT, REF, ?>> initializedSerializersBySerializerClass;

	private final Map<Class<?>, InitializedSerializer<ATTACHMENT, REF, ?>> initializedSerializersByInstanceClass;

	public SerializationHandler(StudentSideCommunicator<ATTACHMENT, REF> communicator, Function<StudentSideInstance, REF> refForStudentSideInstance,
			Function<REF, StudentSideInstance> studentSideInstanceForRef, List<Class<? extends Serializer<?>>> serializerClasses)
	{
		this.communicator = communicator;
		this.refForStudentSideInstance = refForStudentSideInstance;
		this.studentSideInstanceForRef = studentSideInstanceForRef;
		this.serializerClasses = List.copyOf(serializerClasses);

		this.initializedSerializersBySerializerClass = new ConcurrentHashMap<>();
		this.initializedSerializersByInstanceClass = new HashMap<>();
	}
	private SerializationHandler(SerializationHandler<ATTACHMENT, REF> base, List<Class<? extends Serializer<?>>> serializerClasses)
	{
		this.communicator = base.communicator;
		this.refForStudentSideInstance = base.refForStudentSideInstance;
		this.studentSideInstanceForRef = base.studentSideInstanceForRef;
		this.serializerClasses = List.copyOf(serializerClasses);

		this.initializedSerializersBySerializerClass = base.initializedSerializersBySerializerClass;
		this.initializedSerializersByInstanceClass = new HashMap<>();
	}

	public SerializationHandler<ATTACHMENT, REF> withAdditionalSerializers(List<Class<? extends Serializer<?>>> serializerClasses)
	{
		List<Class<? extends Serializer<?>>> mergedSerializerClasses = new ArrayList<>(serializerClasses);
		if(mergedSerializerClasses.isEmpty())
			return this;
		// insert these after new classes to let new serializer classes override old ones
		mergedSerializerClasses.addAll(this.serializerClasses);
		return new SerializationHandler<>(this, mergedSerializerClasses);
	}

	public List<REF> send(List<Class<?>> classes, List<?> objs)
	{
		List<REF> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(send(classes.get(i), objs.get(i)));
		return result;
	}
	public List<?> receive(List<Class<?>> classes, List<REF> objRefs)
	{
		List<Object> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(receive(classes.get(i), objRefs.get(i)));
		return result;
	}

	public <T> REF send(Class<T> clazz, Object obj)
	{
		return sendChecked(clazz, castOrPrimitive(clazz, obj));
	}

	private <T> REF sendChecked(Class<T> clazz, T obj)
	{
		if(obj == null)
			return null;

		//TODO not pretty
		if(StudentSideInstance.class.isAssignableFrom(clazz))
			return refForStudentSideInstance.apply((StudentSideInstance) obj);

		//TODO maybe choose serializer based on dynamic class instead?
		InitializedSerializer<ATTACHMENT, REF, T> serializer = getSerializerForObjectClass(clazz);
		return communicator.send(serializer.serializer(), serializer.studentSideSerializerRef(), obj);
	}

	public <T> T receive(Class<T> clazz, REF objRef)
	{
		return castOrPrimitive(clazz, receiveUnchecked(clazz, objRef));
	}
	private <T> Object receiveUnchecked(Class<T> clazz, REF objRef)
	{
		if(objRef == null)
			return null;

		//TODO not pretty
		if(StudentSideInstance.class.isAssignableFrom(clazz))
			return studentSideInstanceForRef.apply(objRef);

		//TODO maybe choose serializer based on dynamic class instead?
		InitializedSerializer<ATTACHMENT, REF, T> serializer = getSerializerForObjectClass(clazz);
		return communicator.receive(serializer.serializer(), serializer.studentSideSerializerRef(), objRef);
	}

	private <T> InitializedSerializer<ATTACHMENT, REF, T> getSerializerForObjectClass(Class<T> clazz)
	{
		InitializedSerializer<ATTACHMENT, REF, ?> result = initializedSerializersByInstanceClass.computeIfAbsent(clazz, c ->
		{
			for(Class<? extends Serializer<?>> serializerClass : serializerClasses)
			{
				InitializedSerializer<ATTACHMENT, REF, ?> serializer = getSerializerFromSerializerClass(serializerClass);
				if(serializer.serializer().getHandledClass().isAssignableFrom(clazz))
					return serializer;
			}
			throw new SerializationException("No serializer for class " + clazz);
		});
		@SuppressWarnings("unchecked") // this is guaranteed because we only put key-value pairs with matching T
		InitializedSerializer<ATTACHMENT, REF, T> resultCasted = (InitializedSerializer<ATTACHMENT, REF, T>) result;
		return resultCasted;
	}

	private InitializedSerializer<ATTACHMENT, REF, ?> getSerializerFromSerializerClass(Class<? extends Serializer<?>> serializerClass)
	{
		return initializedSerializersBySerializerClass.computeIfAbsent(serializerClass, c ->
		{
			Serializer<?> serializer = ReflectionUtils.callConstructor(serializerClass, List.of(), List.of());
			REF studentSideSerializerRef = communicator.callConstructor(classToName(serializerClass), List.of(), List.of());
			return new InitializedSerializer<>(serializer, studentSideSerializerRef);
		});
	}

	private static record InitializedSerializer<ATTACHMENT, REF extends Ref<ATTACHMENT>, T> (
			Serializer<T> serializer, REF studentSideSerializerRef)
	{}
}
