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
import net.haspamelodica.charon.utils.maps.BidirectionalMap;

public class Marshaler<REF>
{
	private final StudentSideCommunicatorClientSide<REF>	communicator;
	private final RepresentationObjectMarshaler<REF>		representationObjectMarshaler;
	private final List<Class<? extends SerDes<?>>>			serdesClasses;

	//TODO move this to a RefTranslatorCommunicator
	private final BidirectionalMap<Object, REF>	forwardRefs;
	private final BidirectionalMap<Object, REF>	backwardRefs;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	public Marshaler(StudentSideCommunicatorClientSide<REF> communicator,
			RepresentationObjectMarshaler<REF> representationObjectMarshaler, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator;
		this.representationObjectMarshaler = representationObjectMarshaler;
		this.serdesClasses = List.copyOf(serdesClasses);

		//TODO let the user decide if REFs should be stored weakly or identity-based
		this.forwardRefs = BidirectionalMap.builder()
				// If the exercise side doesn't need a student-side object anymore,
				// it should be reclaimed and the exercise side notified.
				.weakKeys()
				.identityKeys()
				.build();
		this.backwardRefs = BidirectionalMap.builder()
				// It is the user's responsibility to keep all Refs to callbacks alive
				// as long as they are needed.
				.weakValues()
				.identityKeys()
				.build();

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler<REF> base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = base.communicator;
		this.representationObjectMarshaler = base.representationObjectMarshaler;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.forwardRefs = base.forwardRefs;
		this.backwardRefs = base.backwardRefs;

		this.initializedSerDesesBySerDesClass = base.initializedSerDesesBySerDesClass;
		// We don't want to inherit those because
		// we might want to use a different serializer for a class than the base.
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}

	public Marshaler<REF> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serdesClasses)
	{
		List<Class<? extends SerDes<?>>> mergedSerDesClasses = new ArrayList<>(serdesClasses);
		if(mergedSerDesClasses.isEmpty())
			return this;
		// insert these after new classes to let new SerDes classes override old ones
		mergedSerDesClasses.addAll(this.serdesClasses);
		return new Marshaler<>(this, mergedSerDesClasses);
	}

	public List<REF> send(List<? extends Class<?>> classes, List<?> objs)
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
		return sendUnchecked(clazz, castOrPrimitive(clazz, obj));
	}

	public <T> REF sendUnchecked(Class<T> clazz, T object)
	{
		if(object == null)
			return null;

		InitializedSerDes<REF, T> serdes = getSerDesForStaticObjectClass(clazz);
		if(serdes != null)
			return communicator.send(serdes.studentSideSerDesRef(), serdes.serdes()::serialize, object);

		// If the passed object is a forward ref, we are sure to find the Ref in this map:
		// it can't have been cleared since it is apparently still reachable,
		// otherwise it couldn't have been passed to this method.
		REF ref = forwardRefs.getValue(object);
		if(ref != null)
			return ref;

		return backwardRefs.computeValueIfAbsent(object, obj ->
		{
			//TODO the callback is new or has been cleared by now; we need to (re)create it
			throw new UnsupportedOperationException("not implemented yet");
		});
	}

	public <T> T receive(Class<T> clazz, REF objRef)
	{
		//TODO make exception easier to understand: this happens if some exercise creator tries to pass an object into a method.
		return castOrPrimitive(clazz, receiveUnchecked(clazz, objRef));
	}
	public <T> Object receiveUnchecked(Class<T> clazz, REF objRef)
	{
		if(objRef == null)
			return null;

		InitializedSerDes<REF, T> serdes = getSerDesForStaticObjectClass(clazz);
		if(serdes != null)
			return communicator.receive(serdes.studentSideSerDesRef(), serdes.serdes()::deserialize, objRef);

		// If the passed Ref is a backward ref (a callback), we are sure to find the object in this map:
		// it can't have been cleared (by the student side) since it is apparently still reachable (by the student side),
		// otherwise the student side wouldn't have passed it to this method.
		Object obj = backwardRefs.getKey(objRef);
		if(obj != null)
			return obj;

		//TODO we need to keep track of the representation object becoming unreachable
		return forwardRefs.computeKeyIfAbsent(objRef, representationObjectMarshaler::createRepresentationObject);
	}

	private <T> InitializedSerDes<REF, T> getSerDesForStaticObjectClass(Class<T> clazz)
	{
		InitializedSerDes<REF, ?> result = initializedSerDesesByInstanceClass.computeIfAbsent(clazz, c ->
		{
			for(Class<? extends SerDes<?>> serdesClass : serdesClasses)
			{
				InitializedSerDes<REF, ?> serdes = getSerDesFromSerDesClass(serdesClass);
				if(serdes.serdes().getHandledClass().isAssignableFrom(clazz))
					return serdes;
			}
			//TODO check if there is a fitting serdes at prototype creation time
			return null;
		});
		@SuppressWarnings("unchecked") // this is guaranteed because we only put key-value pairs with matching T
		InitializedSerDes<REF, T> resultCasted = (InitializedSerDes<REF, T>) result;
		return resultCasted;
	}

	private InitializedSerDes<REF, ?> getSerDesFromSerDesClass(Class<? extends SerDes<?>> serdesClass)
	{
		return initializedSerDesesBySerDesClass.computeIfAbsent(serdesClass, c ->
		{
			SerDes<?> serdes = ReflectionUtils.callConstructor(serdesClass, List.of(), List.of());
			REF serdesRef = communicator.callConstructor(classToName(serdesClass), List.of(), List.of());
			return new InitializedSerDes<>(serdes, serdesRef);
		});
	}

	private static record InitializedSerDes<REF, T>(SerDes<T> serdes, REF studentSideSerDesRef)
	{}
}
