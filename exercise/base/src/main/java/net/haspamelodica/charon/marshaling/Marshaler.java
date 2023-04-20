package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.reflection.ReflectionUtils.castOrPrimitive;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.RefOrError;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.reflection.ExceptionInTargetException;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.util.LazyValue;

public class Marshaler<REF, TYPEREF extends REF, SSX extends StudentSideCausedException>
{
	private final MarshalerCallbacks<REF, TYPEREF, ?, SSX> callbacks;

	private final StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator;

	private final RefTranslator<Object, REF>		translator;
	private final List<Class<? extends SerDes<?>>>	serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	public Marshaler(MarshalerCallbacks<REF, TYPEREF, ?, SSX> callbacks,
			StudentSideCommunicator<REF, TYPEREF, ? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator,
			List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.callbacks = callbacks;
		this.communicator = communicator;
		this.translator = new RefTranslator<>(true, communicator.storeRefsIdentityBased(), new RefTranslatorCallbacks<>()
		{
			@Override
			public Object createForwardRef(REF untranslatedRef)
			{
				return callbacks.createForwardRef(new UntranslatedRef<>(communicator, untranslatedRef));
			}

			@Override
			public REF createBackwardRef(Object translatedRef)
			{
				return communicator.getCallbackManager().createCallbackInstance(callbacks.getCallbackInterfaceCn(translatedRef));
			}
		});
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = new ConcurrentHashMap<>();
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}
	private Marshaler(Marshaler<REF, TYPEREF, SSX> base, List<Class<? extends SerDes<?>>> serdesClasses)
	{
		this.callbacks = base.callbacks;
		this.communicator = base.communicator;
		this.translator = base.translator;
		this.serdesClasses = List.copyOf(serdesClasses);

		this.initializedSerDesesBySerDesClass = base.initializedSerDesesBySerDesClass;
		// We don't want to inherit those because
		// we might want to use a different serializer for a class than the base.
		this.initializedSerDesesByInstanceClass = new HashMap<>();
	}

	public Marshaler<REF, TYPEREF, SSX> withAdditionalSerDeses(List<Class<? extends SerDes<?>>> serdesClasses)
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

	public <T> T receiveOrThrow(Class<T> clazz, RefOrError<REF> objRef) throws SSX
	{
		throwIfError(objRef);
		return receive(clazz, objRef.resultOrErrorRef());
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
			return communicator.getTransceiver().send(serdes.studentSideSerDesRef().get(), serdes.serdes(), object);

		// If the object isn't serializable, it must be a representation object.
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
			return communicator.getTransceiver().receive(serdes.studentSideSerDesRef().get(), serdes.serdes(), objRef);

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

	public void throwIfError(RefOrError<REF> objRef) throws SSX
	{
		if(!objRef.isError())
			return;

		throwIfError_(callbacks, objRef);
	}
	private <SST> void throwIfError_(MarshalerCallbacks<REF, TYPEREF, SST, SSX> callbacks, RefOrError<REF> objRef) throws SSX
	{
		if(objRef.resultOrErrorRef() == null)
			throw new FrameworkCausedException("Error ref refers to null");

		SST studentSideThrowable = callbacks.checkRepresentsStudentSideThrowableAndCastOrNull(translateTo(objRef.resultOrErrorRef()));
		if(studentSideThrowable == null)
			throw new FrameworkCausedException("Error ref doesn't refer to a Throwable, but to an instance of "
					+ communicator.describeType(communicator.getTypeOf(objRef.resultOrErrorRef())).name());

		throw callbacks.newStudentCausedException(studentSideThrowable);
	}

	public boolean isSerializedType(Class<?> clazz)
	{
		return getSerDesForStaticObjectClass(clazz) != null;
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
			SerDes<?> serdes;
			try
			{
				serdes = ReflectionUtils.callConstructor(c, List.of(), List.of());
			} catch(ExceptionInTargetException e)
			{
				throw new ExerciseCausedException("Error while creating SerDes for class " + c, e);
			}
			LazyValue<REF> serdesRef = new LazyValue<>(() ->
			{
				RefOrError<REF> result = communicator.callConstructor(
						communicator.getTypeByName(classToName(c)), List.of(), List.of());
				if(result.isError())
					//TODO maybe we want to make resultOrErrorRef accessible somehow
					throw new StudentSideCausedException("Error while creating student-side SerDes for class " + c);
				return result.resultOrErrorRef();
			});
			return new InitializedSerDes<>(serdes, serdesRef);
		});
	}

	private static record InitializedSerDes<REF, T>(SerDes<T> serdes, LazyValue<REF> studentSideSerDesRef)
	{}
}
