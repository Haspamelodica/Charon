package net.haspamelodica.charon.communicator.impl.logging;

import java.util.function.BiFunction;

import net.haspamelodica.charon.communicator.CallbackManager;
import net.haspamelodica.charon.communicator.StudentSideCommunicator;
import net.haspamelodica.charon.communicator.StudentSideCommunicatorCallbacks;
import net.haspamelodica.charon.communicator.Transceiver;
import net.haspamelodica.charon.communicator.UninitializedStudentSideCommunicator;

public class LoggingUninitializedStudentSideCommunicator<REF, TYPEREF extends REF, TC extends Transceiver, CM extends CallbackManager>
		implements UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM>
{
	private final CommunicationLoggerParams										loggerParams;
	private final UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM>	communicator;
	private final BiFunction<CommunicationLogger<TYPEREF>, TC, TC>				wrapTransceiverLogging;
	private final BiFunction<CommunicationLogger<TYPEREF>, CM, CM>				wrapCallbackManagerLogging;

	public LoggingUninitializedStudentSideCommunicator(CommunicationLoggerParams loggerParams,
			UninitializedStudentSideCommunicator<REF, TYPEREF, TC, CM> communicator,
			BiFunction<CommunicationLogger<TYPEREF>, TC, TC> wrapTransceiverLogging,
			BiFunction<CommunicationLogger<TYPEREF>, CM, CM> wrapCallbackManagerLogging)
	{
		this.loggerParams = loggerParams;
		this.communicator = communicator;
		this.wrapTransceiverLogging = wrapTransceiverLogging;
		this.wrapCallbackManagerLogging = wrapCallbackManagerLogging;
	}

	@Override
	public StudentSideCommunicator<REF, TYPEREF, ? extends TC, ? extends CM> initialize(StudentSideCommunicatorCallbacks<REF, TYPEREF> callbacks)
	{
		return new LoggingCommunicator<REF, TYPEREF, TC, CM>(loggerParams,
				logger -> communicator.initialize(new LoggingStudentSideCommunicatorCallbacks<>(logger, callbacks)),
				(logger, communicator) -> wrapTransceiverLogging.apply(logger, communicator.getTransceiver()),
				(logger, communicator) -> wrapCallbackManagerLogging.apply(logger, communicator.getCallbackManager()));
	}
}
