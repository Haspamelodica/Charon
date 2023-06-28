package net.haspamelodica.charon.marshaling;

import static net.haspamelodica.charon.reflection.ReflectionUtils.castOrPrimitive;
import static net.haspamelodica.charon.reflection.ReflectionUtils.classToName;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import net.haspamelodica.charon.OperationKind;
import net.haspamelodica.charon.OperationOutcome;
import net.haspamelodica.charon.communicator.ClientSideTransceiver;
import net.haspamelodica.charon.communicator.InternalCallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslator;
import net.haspamelodica.charon.communicator.impl.reftranslating.RefTranslatorCallbacks;
import net.haspamelodica.charon.communicator.impl.reftranslating.UntranslatedRef;
import net.haspamelodica.charon.exceptions.CharonException;
import net.haspamelodica.charon.exceptions.ExerciseCausedException;
import net.haspamelodica.charon.exceptions.FrameworkCausedException;
import net.haspamelodica.charon.exceptions.HierarchyMismatchException;
import net.haspamelodica.charon.exceptions.StudentSideCausedException;
import net.haspamelodica.charon.reflection.ReflectionUtils;
import net.haspamelodica.charon.util.LazyValue;
import net.haspamelodica.charon.utils.maps.BidirectionalMap;
import net.haspamelodica.charon.utils.maps.UnidirectionalMap;

public class Marshaler<REF, TYPEREF extends REF, SSX extends StudentSideCausedException>
{
	private final MarshalerCallbacks<REF, TYPEREF, ?, SSX> callbacks;

	private final StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?,
			? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator;

	private final RefTranslator<Object, REF>		translator;
	private final List<Class<? extends SerDes<?>>>	serdesClasses;

	private final ConcurrentMap<Class<? extends SerDes<?>>, InitializedSerDes<REF, ?>> initializedSerDesesBySerDesClass;

	private final Map<Class<?>, InitializedSerDes<REF, ?>> initializedSerDesesByInstanceClass;

	private final UnidirectionalMap<Class<?>, BidirectionalMap<REF, Object>> cachedPrimitives;

	public Marshaler(MarshalerCallbacks<REF, TYPEREF, ?, SSX> callbacks,
			StudentSideCommunicator<REF, ?, TYPEREF, ?, ?, ?,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator,
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
		//TODO why not concurrent?
		this.initializedSerDesesByInstanceClass = new HashMap<>();
		this.cachedPrimitives = UnidirectionalMap.builder().concurrent().build();
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
		//TODO not keeping any cached primitives is suboptimal, but we need to be careful if a user overrides the default primitive serializers.
		// We could also just disallow that.
		this.cachedPrimitives = UnidirectionalMap.builder().concurrent().build();
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

	public <T> T receiveOrThrowVoid(OperationKind operationKind, Class<T> clazz, OperationOutcome<REF, Void, TYPEREF> outcome)
	{
		return receive(clazz, handleOperationOutcomeVoid(operationKind, outcome));
	}

	public <T> T receiveOrThrow(OperationKind operationKind, Class<T> clazz, OperationOutcome<REF, ? extends REF, TYPEREF> outcome) throws SSX
	{
		return receive(clazz, handleOperationOutcome(operationKind, outcome));
	}

	public <RESULTREF, THROWABLEREF extends REF> RESULTREF handleOperationOutcome(OperationKind operationKind,
			OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF> outcome) throws SSX
	{
		// The Eclipse compiler is able to deduce that this can only throw SSX, not Exception, javac is not.
		// Probably a bug in the Eclipse compiler.
		// Workaround: Specify the type arguments to the method explicitly.
		return Marshaler.<SSX, RESULTREF, THROWABLEREF, TYPEREF> handleOperationOutcome(operationKind, outcome,
				thrown -> throwSSX(callbacks, thrown));
	}

	private <R, THROWABLEREF extends REF, SST> R throwSSX(MarshalerCallbacks<REF, TYPEREF, SST, SSX> callbacks,
			OperationOutcome.Thrown<?, THROWABLEREF, TYPEREF> outcome) throws SSX
	{
		SST studentSideThrowable = callbacks.checkRepresentsStudentSideThrowableAndCastOrNull(translateTo(outcome.thrownThrowable()));
		if(studentSideThrowable == null)
			throw new FrameworkCausedException("Error ref doesn't refer to a Throwable, but to an instance of "
					+ communicator.describeType(communicator.getTypeOf(outcome.thrownThrowable())).name());

		throw callbacks.newStudentCausedException(studentSideThrowable);
	}

	public <T> REF send(Class<T> clazz, Object obj)
	{
		return sendUnchecked(clazz, castOrPrimitive(clazz, obj));
	}

	public <T> REF sendUnchecked(Class<T> clazz, T object)
	{
		if(object == null)
			return null;

		//TODO this should even work for all immutables, but keep an eye on object identity
		if(clazz.isPrimitive())
			return lookupPrimitiveForPrimitiveType(clazz).computeKeyIfAbsent(object, o -> sendUncached(clazz, object));

		return sendUncached(clazz, object);
	}
	private <T> REF sendUncached(Class<T> clazz, T object)
	{
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

		//TODO this should even work for all immutables, but keep an eye on object identity
		if(clazz.isPrimitive())
			return lookupPrimitiveForPrimitiveType(clazz).computeValueIfAbsent(objRef, o -> receiveUncached(clazz, objRef));

		return receiveUncached(clazz, objRef);
	}
	private <T> Object receiveUncached(Class<T> clazz, REF objRef)
	{
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

	private <T> BidirectionalMap<REF, Object> lookupPrimitiveForPrimitiveType(Class<T> clazz)
	{
		return cachedPrimitives.computeIfAbsent(clazz,
				c -> BidirectionalMap.builder().concurrent().identityKeys(communicator.storeRefsIdentityBased()).build());
	}

	public TYPEREF getTypeHandledByStudentSideSerdes(Class<? extends SerDes<?>> serdesClass)
	{
		return communicator.getTypeHandledBySerdes(getSerDesFromSerDesClass(serdesClass).studentSideSerDesRef().get());
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
			OperationOutcome<Constructor<?>, Void, Class<?>> localSerdesConstructorLookupOutcome =
					ReflectionUtils.lookupConstructor(c, List.of());
			Constructor<?> localSerdesConstructor =
					handleOperationOutcomeVoid(OperationKind.LOOKUP_CONSTRUCTOR, localSerdesConstructorLookupOutcome);
			OperationOutcome<Object, Throwable, Class<?>> localSerdesOutcome =
					ReflectionUtils.callConstructor(localSerdesConstructor, List.of());
			SerDes<?> serdes = (SerDes<?>) handleOperationOutcome(OperationKind.CALL_CONSTRUCTOR, localSerdesOutcome,
					thrown -> new ExerciseCausedException("Error while creating SerDes for class " + c, thrown.thrownThrowable()));

			LazyValue<REF> serdesRef = new LazyValue<REF>(() -> createStudentSerdes(c, communicator));
			return new InitializedSerDes<>(serdes, serdesRef);
		});
	}
	private <CONSTRUCTORREF extends REF> REF createStudentSerdes(Class<? extends SerDes<?>> c,
			StudentSideCommunicator<REF, ?, TYPEREF, CONSTRUCTORREF, ?, ?,
					? extends ClientSideTransceiver<REF>, ? extends InternalCallbackManager<REF>> communicator)
	{
		TYPEREF serdesTyperef;
		try
		{
			serdesTyperef = handleOperationOutcomeVoid(OperationKind.GET_TYPE_BY_NAME, communicator.getTypeByName(classToName(c)));
		} catch(CharonException e)
		{
			// This can only be CLASS_NOT_FOUND
			throw new StudentSideCausedException("Error while looking up student-side SerDes " + c, e);
		}
		CONSTRUCTORREF serdesConstructorref;
		try
		{
			serdesConstructorref = handleOperationOutcomeVoid(OperationKind.LOOKUP_CONSTRUCTOR,
					communicator.lookupConstructor(serdesTyperef, List.of()));
		} catch(CharonException e)
		{
			throw new StudentSideCausedException("Error while looking up student-side constructor of SerDes " + c, e);
		}
		try
		{
			return handleOperationOutcome(OperationKind.CALL_CONSTRUCTOR, communicator.callConstructor(serdesConstructorref, List.of()));
		} catch(CharonException e)
		{
			throw new StudentSideCausedException("Error while creating student-side SerDes " + c, e);
		}
	}

	public static <RESULTREF, TYPEREF> RESULTREF handleOperationOutcomeVoid(
			OperationKind operationKind, OperationOutcome<RESULTREF, Void, TYPEREF> outcome)
	{
		return handleOperationOutcome(operationKind, outcome, thrown ->
		{
			throw new FrameworkCausedException(operationKind + " outcome was of kind " + OperationOutcome.Kind.THROWN);
		});
	}

	public static <X extends Exception, RESULTREF, THROWABLEREF, TYPEREF> RESULTREF handleOperationOutcome(OperationKind operationKind,
			OperationOutcome<RESULTREF, THROWABLEREF, TYPEREF> outcome,
			Function<OperationOutcome.Thrown<RESULTREF, THROWABLEREF, TYPEREF>, X> wrapThrown) throws X
	{
		operationKind.checkOutcomeAllowed(outcome);
		return switch(outcome.kind())
		{
			case RESULT -> ((OperationOutcome.Result<RESULTREF, THROWABLEREF, TYPEREF>) outcome).returnValue();
			case SUCCESS_WITHOUT_RESULT -> null;
			case THROWN -> throw wrapThrown.apply((OperationOutcome.Thrown<RESULTREF, THROWABLEREF, TYPEREF>) outcome);
			// TODO better error messages for the cases below; for this, extract some toString code from CommunicationLogger to OperationOutcome
			case CLASS_NOT_FOUND -> throw new HierarchyMismatchException("class not found: " + outcome);
			case FIELD_NOT_FOUND -> throw new HierarchyMismatchException("field not found: " + outcome);
			case METHOD_NOT_FOUND -> throw new HierarchyMismatchException("method not found: " + outcome);
			case CONSTRUCTOR_NOT_FOUND -> throw new HierarchyMismatchException("constructor not found: " + outcome);
			case CONSTRUCTOR_OF_ABSTRACT_CLASS_CREATED -> throw new HierarchyMismatchException("constructor of abstract created: " + outcome);
			case ARRAY_INDEX_OUT_OF_BOUNDS -> throw new ExerciseCausedException("Array index out of bounds: " + outcome);
			case ARRAY_SIZE_NEGATIVE -> throw new ExerciseCausedException("Array size negative: " + outcome);
			case ARRAY_SIZE_NEGATIVE_IN_MULTI_ARRAY -> throw new ExerciseCausedException("Array size negative for multiarray: " + outcome);
		};
	}

	private static record InitializedSerDes<REF, T>(SerDes<T> serdes, LazyValue<REF> studentSideSerDesRef)
	{}
}
