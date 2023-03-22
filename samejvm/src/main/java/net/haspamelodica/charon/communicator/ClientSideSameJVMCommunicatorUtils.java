package net.haspamelodica.charon.communicator;

import java.util.function.Function;

import net.haspamelodica.charon.communicator.impl.data.exercise.DataCommunicatorClient;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMClientSideTransceiver;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicator;
import net.haspamelodica.charon.impl.StudentSideImpl;

public class ClientSideSameJVMCommunicatorUtils
{
	/**
	 * <b>Using this method in the tester JVM to create a {@link StudentSideImpl} is not safe
	 * as it gives student code full access to the system the tester JVM is running on.</b>
	 * Use a {@link DataCommunicatorClient} instead.
	 * <p>
	 * For more details, see {@link DirectSameJVMClientSideTransceiver}.
	 */
	public static StudentSideCommunicator<Object, ? extends ClientSideTransceiver<Object>, ? extends InternalCallbackManager<Object>>
			createDirectCommClient(StudentSideCommunicatorCallbacks<Object> callbacks)
	{
		return new DirectSameJVMCommunicator<>(callbacks, directTcClient());
	}

	/**
	 * <b>Using this method in the tester JVM to create a {@link StudentSideImpl} is not safe
	 * as it gives student code full access to the system the tester JVM is running on.</b>
	 * Use a {@link DataCommunicatorClient} instead.
	 * <p>
	 * For more details, see {@link DirectSameJVMClientSideTransceiver}.
	 */
	public static UninitializedStudentSideCommunicator<Object, ClientSideTransceiver<Object>, InternalCallbackManager<Object>>
			createDirectCommClient()
	{
		return DirectSameJVMCommunicator.createUninitializedCommunicator(directTcClient());
	}

	private static Function<StudentSideCommunicatorCallbacks<Object>, ClientSideTransceiver<Object>> directTcClient()
	{
		return DirectSameJVMClientSideTransceiver::new;
	}

	private ClientSideSameJVMCommunicatorUtils()
	{}
}
