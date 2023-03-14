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
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.reflection.ReflectionUtils;

public class Marshaler<REF>
{
	private final StudentSideCommunicatorClientSide<REF>	communicator;
	private final RefTranslator<Object, REF>				translator;
	private final List<Class<? extends SerDes<?>>>			serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	public Marshaler(StudentSideCommunicatorClientSide<REF> communicator, RepresentationObjectMarshaler representationObjectMarshaler,
			List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = communicator;
		this.translator = new RefTranslator<>(true, communicator.storeRefsIdentityBased(), new RefTranslatorCallbacks<>()
		{
			@Override
			public Object createForwardRef(REF untranslatedRef)
			{
				return representationObjectMarshaler.createRepresentationObject(new UntranslatedRef<>(communicator, untranslatedRef));
			}

			@Override
			public REF createBackwardRef(Object translatedRef)
			{
				return communicator.createCallbackInstance(representationObjectMarshaler.getCallbackInterfaceCn(translatedRef));
			}
		});
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler<REF> base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.communicator = base.communicator;
		this.translator = base.translator;
		this.serdesClasses = List.copyOf(serdesClasses);

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
	public List<Object> receive(List<Class<?>> classes, List<REF> objRefs)
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

		return translateFrom(object);
	}
	public REF translateFrom(Object object)
	{
		return translator.translateFrom(object);
	}
	public List<REF> translateFrom(List<Object> objects)
	{
		return translator.translateFrom(objects);
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

		return translateTo(objRef);
	}
	public Object translateTo(REF objRef)
	{
		return translator.translateTo(objRef);
	}
	public List<Object> translateTo(List<REF> objRefs)
	{
		return translator.translateTo(objRefs);
	}

	public void setRepresentationObjectRefPair(REF ref, Object representationObject)
	{
		translator.setForwardRefTranslation(ref, representationObject);
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
