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
import net.haspamelodica.charon.exceptions.MissingSerDesException;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.refs.Ref;

public class Marshaler<REFR, REPR extends REFR, REF extends Ref<?, REFR>>
{
	private final StudentSideCommunicatorClientSide<REF>			communicator;
	private final RepresentationObjectMarshaler<REFR, REPR, REF>	representationObjectMarshaler;
	private final List<Class<? extends SerDes<?>>>					serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	public Marshaler(StudentSideCommunicatorClientSide<REF> communicator,
			RepresentationObjectMarshaler<REFR, REPR, REF> representationObjectMarshaler, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator;
		this.representationObjectMarshaler = representationObjectMarshaler;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler<REFR, REPR, REF> base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = base.communicator;
		this.representationObjectMarshaler = base.representationObjectMarshaler;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = base.initializedSerDesesBySerDesClass;
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}

	public Marshaler<REFR, REPR, REF> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serdesClasses)
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
		return sendChecked(clazz, castOrPrimitive(clazz, obj));
	}

	private <T> REF sendChecked(Class<T> clazz, T obj)
	{
		if(obj == null)
			return null;

		if(representationObjectMarshaler.representationObjectClass().isAssignableFrom(clazz))
		{
			@SuppressWarnings("unchecked") // checked with isAssignableFrom on clazz
			Class<? extends REPR> representationObjectClass = (Class<? extends REPR>) clazz;
			if(representationObjectMarshaler.isRepresentationObjectClass(representationObjectClass))
			{
				// checked with isAssignableFrom on clazz; caller is responsible for obj being an instance of clazz
				@SuppressWarnings("unchecked")
				REPR representationObject = (REPR) obj;
				// don't set referrer here: representationObjectMarshaler is responsible.
				return representationObjectMarshaler.marshal(representationObject);
			}
		}

		//TODO maybe choose SerDes based on dynamic class instead?
		InitializedSerDes<REF, T> serdes = getSerDesForObjectClass(clazz);
		return communicator.send(serdes.studentSideSerDesRef(), serdes.serdes()::serialize, obj);
	}

	public <T> T receive(Class<T> clazz, REF objRef)
	{
		return castOrPrimitive(clazz, receiveUnchecked(clazz, objRef));
	}
	private <T> Object receiveUnchecked(Class<T> clazz, REF objRef)
	{
		if(objRef == null)
			return null;

		// If the ref already has a referrer (a representation object or a callback) set, use it.
		// This is important to ensure == works on representation objects, and to catch backward references.
		// Unsynchronized fast path: catches all callbacks and most already-created representation objects.
		Object referrer = objRef.referrer();
		if(referrer != null)
			return referrer;

		synchronized(objRef)
		{
			// synchronized slow path; re-check if another thread was faster
			referrer = objRef.referrer();
			if(referrer != null)
				return referrer;

			// nope; we are the thread responsible for creating a representation object.
			// Also, we are sure objRef's referrer should be a representation object: callbacks always have a referrer set.

			if(representationObjectMarshaler.representationObjectClass().isAssignableFrom(clazz))
			{
				@SuppressWarnings("unchecked") // checked with isAssignableFrom on clazz
				Class<? extends REPR> representationObjectClass = (Class<? extends REPR>) clazz;
				if(representationObjectMarshaler.isRepresentationObjectClass(representationObjectClass))
				{
					REFR representationObject = representationObjectMarshaler.unmarshal(objRef);
					objRef.setReferrer(representationObject);
					return representationObject;
				}
			}
		}

		// Don't write deserialized object into referrer: deserialized object is not a representation of the student-side object.
		// This is because the student-side object might change; then we want to reserialize.
		//TODO maybe choose SerDes based on dynamic class instead?
		InitializedSerDes<REF, T> serdes = getSerDesForObjectClass(clazz);
		return communicator.receive(serdes.studentSideSerDesRef(), serdes.serdes()::deserialize, objRef);
	}

	private <T> InitializedSerDes<REF, T> getSerDesForObjectClass(Class<T> clazz)
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
			//TODO make exception easier to understand: this happens if some exercise creator tries to pass any object into a method.
			throw new MissingSerDesException("No SerDes for class " + clazz);
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

	private static record InitializedSerDes<REF extends Ref<?, ?>, T> (
			SerDes<T> serdes, REF studentSideSerDesRef)
	{}
}
