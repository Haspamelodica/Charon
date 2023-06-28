package net.haspamelodica.charon.communicator.impl.samejvm;

import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;

public abstract class DirectSameJVMTransceiverImpl implements Transceiver
{
	protected final StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks;

	protected DirectSameJVMTransceiverImpl(StudentSideCommunicatorCallbacks<Object, Throwable, Class<?>> callbacks)
	{
		this.callbacks = callbacks;
	}
}
