package net.haspamelodica.charon.communicator.impl.logging;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class LoggingUninitializedStudentSideCommunicator<REF,
		THROWABLEREF extends REF, TYPEREF extends REF, CONSTRUCTORREF extends REF, METHODREF extends REF, FIELDREF extends REF,
		TC extends Transceiver, CM extends CallbackManager>
		implements UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>
{
	private final CommunicationLoggerParams loggerParams;

	private final UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM> communicator;

	private final BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC>	wrapTransceiverLogging;
	private final BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM>	wrapCallbackManagerLogging;

	public LoggingUninitializedStudentSideCommunicator(CommunicationLoggerParams loggerParams,
			UninitializedStudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM> communicator,
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, TC, TC> wrapTransceiverLogging,
			BiFunction<CommunicationLogger<REF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF>, CM, CM> wrapCallbackManagerLogging)
	{
		this.loggerParams = loggerParams;
		this.communicator = communicator;
		this.wrapTransceiverLogging = wrapTransceiverLogging;
		this.wrapCallbackManagerLogging = wrapCallbackManagerLogging;
	}

	@Override
	public StudentSideCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, ? extends TC, ? extends CM>
			initialize(StudentSideCommunicatorCallbacks<REF, THROWABLEREF, TYPEREF> callbacks)
	{
		return new LoggingCommunicator<REF, THROWABLEREF, TYPEREF, CONSTRUCTORREF, METHODREF, FIELDREF, TC, CM>(loggerParams,
				logger -> communicator.initialize(new LoggingStudentSideCommunicatorCallbacks<>(logger, callbacks)),
				(logger, communicator) -> wrapTransceiverLogging.apply(logger, communicator.getTransceiver()),
				(logger, communicator) -> wrapCallbackManagerLogging.apply(logger, communicator.getCallbackManager()));
	}
}
