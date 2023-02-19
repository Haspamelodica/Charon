package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.reflection.ReflectionUtils.castOrPrimitive;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class Marshaler
{
	private final StudentSideCommunicatorClientSide<Object>	communicator;
	private final List<Class<? extends SerDes<?>>>			serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<?>> initializedSerDesesByInstanceClass;

	public Marshaler(StudentSideCommunicatorClientSide<Object> communicator, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = base.communicator;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = base.initializedSerDesesBySerDesClass;
		// We don't want to inherit those because
		// we might want to use a different serializer for a class than the base.
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}

	public Marshaler withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serdesClasses)
	{
		List<Class<? extends SerDes<?>>> mergedSerDesClasses = new ArrayList<>(serdesClasses);
		if(mergedSerDesClasses.isEmpty())
			return this;
		// insert these after new classes to let new SerDes classes override old ones
		mergedSerDesClasses.addAll(this.serdesClasses);
		return new Marshaler(this, mergedSerDesClasses);
	}

	public List<Object> send(List<? extends Class<?>> classes, List<?> objs)
	{
		List<Object> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(send(classes.get(i), objs.get(i)));
		return result;
	}
	public List<?> receive(List<Class<?>> classes, List<Object> objRefs)
	{
		List<Object> result = new ArrayList<>();
		for(int i = 0; i < classes.size(); i ++)
			result.add(receive(classes.get(i), objRefs.get(i)));
		return result;
	}

	public <T> Object send(Class<T> clazz, Object obj)
	{
		return sendUnchecked(clazz, castOrPrimitive(clazz, obj));
	}

	public <T> Object sendUnchecked(Class<T> clazz, T object)
	{
		if(object == null)
			return null;

		InitializedSerDes<T> serdes = getSerDesForStaticObjectClass(clazz);
		if(serdes != null)
			return communicator.send(serdes.studentSideSerDesRef(), serdes.serdes()::serialize, object);

		return object;
	}

	public <T> T receive(Class<T> clazz, Object objRef)
	{
		//TODO make exception easier to understand: this happens if some exercise creator tries to pass an object into a method.
		return castOrPrimitive(clazz, receiveUnchecked(clazz, objRef));
	}
	public <T> Object receiveUnchecked(Class<T> clazz, Object objRef)
	{
		if(objRef == null)
			return null;

		InitializedSerDes<T> serdes = getSerDesForStaticObjectClass(clazz);
		if(serdes != null)
			return communicator.receive(serdes.studentSideSerDesRef(), serdes.serdes()::deserialize, objRef);

		return objRef;
	}

	private <T> InitializedSerDes<T> getSerDesForStaticObjectClass(Class<T> clazz)
	{
		InitializedSerDes<?> result = initializedSerDesesByInstanceClass.computeIfAbsent(clazz, c ->
		{
			for(Class<? extends SerDes<?>> serdesClass : serdesClasses)
			{
				InitializedSerDes<?> serdes = getSerDesFromSerDesClass(serdesClass);
				if(serdes.serdes().getHandledClass().isAssignableFrom(clazz))
					return serdes;
			}
			//TODO check if there is a fitting serdes at prototype creation time
			return null;
		});
		@SuppressWarnings("unchecked") // this is guaranteed because we only put key-value pairs with matching T
		InitializedSerDes<T> resultCasted = (InitializedSerDes<T>) result;
		return resultCasted;
	}

	private InitializedSerDes<?> getSerDesFromSerDesClass(Class<? extends SerDes<?>> serdesClass)
	{
		return initializedSerDesesBySerDesClass.computeIfAbsent(serdesClass, c ->
		{
			SerDes<?> serdes = ReflectionUtils.callConstructor(serdesClass, List.of(), List.of());
			//TODO this does not work. callConstructor will attempt to unmarshal the serdes, which won't work since there is no SSI for serializers.
			Object serdesRef = communicator.callConstructor(classToName(serdesClass), List.of(), List.of());
			return new InitializedSerDes<>(serdes, serdesRef);
		});
	}

	private static record InitializedSerDes<T>(SerDes<T> serdes, Object studentSideSerDesRef)
	{}
}
