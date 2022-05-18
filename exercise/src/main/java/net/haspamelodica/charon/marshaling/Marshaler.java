package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.reflection.ReflectionUtils.castOrPrimitive;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import net.haspamelodica.charon.StudentSideInstance;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorClientSide;
import net.haspamelodica.charon.exceptions.MissingSerDesException;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.refs.Ref;

public class Marshaler<REF extends Ref<?, ?>>
{
	private final StudentSideCommunicatorClientSide<REF>	communicator;
	private final Function<StudentSideInstance, REF>		refForStudentSideInstance;
	private final Function<REF, StudentSideInstance>		createStudentSideInstanceForRef;
	private final List<Class<? extends SerDes<?>>>			serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	public Marshaler(StudentSideCommunicatorClientSide<REF> communicator, Function<StudentSideInstance, REF> refForStudentSideInstance,
			Function<REF, StudentSideInstance> createStudentSideInstanceForRef, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator;
		this.refForStudentSideInstance = refForStudentSideInstance;
		this.createStudentSideInstanceForRef = createStudentSideInstanceForRef;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler<REF> base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = base.communicator;
		this.refForStudentSideInstance = base.refForStudentSideInstance;
		this.createStudentSideInstanceForRef = base.createStudentSideInstanceForRef;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = base.initializedSerDesesBySerDesClass;
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
		return sendChecked(clazz, castOrPrimitive(clazz, obj));
	}

	private <T> REF sendChecked(Class<T> clazz, T obj)
	{
		if(obj == null)
			return null;

		//TODO not pretty
		if(StudentSideInstance.class.isAssignableFrom(clazz))
			return refForStudentSideInstance.apply((StudentSideInstance) obj);

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

		// If the ref already has a referrer set, use it.
		// This is important to ensure == works on SSIs, and to catch backward references.
		// Unsynchronized fast path: catches all backward references and most already-created SSIs.
		Object referrer = objRef.referrer();
		if(referrer != null)
			return referrer;

		// objRef is a forward reference: backward references always have a referrer set.

		//TODO not pretty
		if(StudentSideInstance.class.isAssignableFrom(clazz))
			synchronized(objRef)
			{
				// Re-get referrer: another thread might have been faster
				return createStudentSideInstanceForRef.apply(objRef);
			}

		// Don't write deserialized object into referrer: object might change; then we want to reserialize.
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
			throw new MissingSerDesException("No serdes for class " + clazz);
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
