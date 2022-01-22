package net.haspamelodica.studentcodeseparator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.haspamelodica.studentcodeseparator.Serializer;
import net.haspamelodica.studentcodeseparator.communicator.StudentSideCommunicator;
import net.haspamelodica.studentcodeseparator.exceptions.SerializationException;
import net.haspamelodica.studentcodeseparator.reflection.ReflectionUtils;
import net.haspamelodica.studentcodeseparator.serializers.PrimitiveSerializer;

public class SerializationHandler<REF>
{
	private final StudentSideCommunicator<REF>			communicator;
	private final List<Class<? extends Serializer<?>>>	serializerClasses;

	private final ConcurrentMap<Class<? extends Serializer<?>>, InitializedSerializer<REF, ?>> initializedSerializersBySerializerClass;

	private final Map<Class<?>, InitializedSerializer<REF, ?>> initializedSerializersByObjectClass;

	public SerializationHandler(StudentSideCommunicator<REF> communicator)
	{
		this.communicator = communicator;
		this.serializerClasses = PrimitiveSerializer.PRIMITIVE_SERIALIZERS;

		this.initializedSerializersBySerializerClass = new ConcurrentHashMap<>();
		this.initializedSerializersByObjectClass = new HashMap<>();
	}
	private SerializationHandler(StudentSideCommunicator<REF> communicator, List<Class<? extends Serializer<?>>> serializerClasses,
			ConcurrentMap<Class<? extends Serializer<?>>, InitializedSerializer<REF, ?>> initializedSerializersBySerializerClass)
	{
		this.communicator = communicator;
		this.serializerClasses = List.copyOf(serializerClasses);

		this.initializedSerializersBySerializerClass = initializedSerializersBySerializerClass;
		this.initializedSerializersByObjectClass = new HashMap<>();
	}

	public SerializationHandler<REF> withAdditionalSerializers(List<Class<? extends Serializer<?>>> serializerClasses)
	{
		List<Class<? extends Serializer<?>>> mergedSerializerClasses = new ArrayList<>(serializerClasses);
		if(mergedSerializerClasses.isEmpty())
			return this;
		//insert these after new classes to let new serializer classes override old ones
		mergedSerializerClasses.addAll(this.serializerClasses);
		return new SerializationHandler<>(communicator, mergedSerializerClasses, this.initializedSerializersBySerializerClass);
	}

	public List<REF> send(List<Class<?>> classes, List<?> objs)
	{
		List<REF> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(sendUnsafe(classes.get(i), objs.get(i)));
		return result;
	}
	public List<?> receive(List<Class<?>> classes, List<REF> objRefs)
	{
		List<Object> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(receive(classes.get(i), objRefs.get(i)));
		return result;
	}

	public <T> REF sendUnsafe(Class<T> clazz, Object obj)
	{
		@SuppressWarnings("unchecked") // responsibility of caller
		T objCasted = (T) obj;
		return send(clazz, objCasted);
	}

	public <T> REF send(Class<T> clazz, T obj)
	{
		InitializedSerializer<REF, T> serializer = getSerializerForObjectClass(clazz);
		return communicator.send(serializer.serializer(), serializer.studentSideSerializerRef(), obj);
	}

	public <T> T receive(Class<T> clazz, REF objRef)
	{
		InitializedSerializer<REF, T> serializer = getSerializerForObjectClass(clazz);
		return communicator.receive(serializer.serializer(), serializer.studentSideSerializerRef(), objRef);
	}

	private <T> InitializedSerializer<REF, T> getSerializerForObjectClass(Class<T> clazz)
	{
		InitializedSerializer<REF, ?> result = initializedSerializersByObjectClass.computeIfAbsent(clazz, c ->
		{
			for(Class<? extends Serializer<?>> serializerClass : serializerClasses)
			{
				InitializedSerializer<REF, ?> serializer = getSerializerFromSerializerClass(serializerClass);
				if(serializer.serializer().getHandledClass().isAssignableFrom(clazz))
					return serializer;
			}
			throw new SerializationException("No serializer for class " + clazz);
		});
		@SuppressWarnings("unchecked") // this is guaranteed because we only put key-value pairs with matching T
		InitializedSerializer<REF, T> resultCasted = (InitializedSerializer<REF, T>) result;
		return resultCasted;
	}

	private InitializedSerializer<REF, ?> getSerializerFromSerializerClass(Class<? extends Serializer<?>> serializerClass)
	{
		return initializedSerializersBySerializerClass.computeIfAbsent(serializerClass, c ->
		{
			Serializer<?> serializer = ReflectionUtils.callConstructor(serializerClass, List.of(), List.of());
			REF studentSideSerializerRef = communicator.callConstructor(serializerClass.getName(), List.of(), List.of());
			return new InitializedSerializer<>(serializer, studentSideSerializerRef);
		});
	}

	private static record InitializedSerializer<REF, T> (Serializer<T> serializer, REF studentSideSerializerRef)
	{}
}
