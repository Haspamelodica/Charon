package net.haspamelodica.charon.communicator;

import java.util.function.Function;

import net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMClientSideTransceiver;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicator;

public class ClientSideSameJVMCommunicatorUtils
{
	/**
	 * <b>Using this method is not safe
	 * as it gives student code full access to the system the tester JVM is running on.</b>
	 * Use a {@link DataCommunicatorClient} instead.
	 * <p>
	 * For more details, see {@link DirectSameJVMClientSideTransceiver}.
	 */
	public static StudentSideCommunicator<Object, Class<?>, ? extends ClientSideTransceiver<Object>, ? extends InternalCallbackManager<Object>>
			createDirectCommClient(StudentSideCommunicatorCallbacks<Object, Class<?>> callbacks)
	{
		return new DirectSameJVMCommunicator<>(callbacks, directTcClient());
	}

	/**
	 * <b>Using this method is not safe
	 * as it gives student code full access to the system the tester JVM is running on.</b>
	 * Use a {@link DataCommunicatorClient} instead.
	 * <p>
	 * For more details, see {@link DirectSameJVMClientSideTransceiver}.
	 */
	public static UninitializedStudentSideCommunicator<Object, Class<?>, ClientSideTransceiver<Object>, InternalCallbackManager<Object>>
			createDirectCommClient()
	{
		return DirectSameJVMCommunicator.createUninitializedCommunicator(directTcClient());
	}

	private static Function<StudentSideCommunicatorCallbacks<Object, Class<?>>, ClientSideTransceiver<Object>> directTcClient()
	{
		return DirectSameJVMClientSideTransceiver::new;
	}

	private ClientSideSameJVMCommunicatorUtils()
	{}
}
