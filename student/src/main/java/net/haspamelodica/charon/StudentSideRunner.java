package net.haspamelodica.charon;

import static net.haspamelodica.charon.communicator.impl.LoggingCommunicatorServerSide.maybeWrapLoggingS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.haspamelodica.charon.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.charon.communicator.impl.samejvm.DirectSameJVMCommunicatorServerSide;
import net.haspamelodica.charon.refs.direct.WeakDirectRefManager;
import net.haspamelodica.charon.utils.communication.Communication;
import net.haspamelodica.charon.utils.communication.CommunicationArgsParser;
import net.haspamelodica.charon.utils.communication.IncorrectUsageException;

public class StudentSideRunner
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		try(Communication communication = Communication.open(CommunicationArgsParser.parse(args)))
		{
			run(communication);
		} catch(IncorrectUsageException e)
		{
			e.printStackTrace();
			System.err.println("Usage: java " + StudentSideRunner.class.getName() + CommunicationArgsParser.argsSyntax());
		}
	}

	public static void run(Communication communication) throws IOException
	{
		run(communication.getIn(), communication.getOut(), communication.getLogging());
	}

	public static void run(InputStream in, OutputStream out, boolean logging) throws IOException
	{
		DataCommunicatorServer server = new DataCommunicatorServer(in, out,
				maybeWrapLoggingS(new DirectSameJVMCommunicatorServerSide(new WeakDirectRefManager()), logging));
		server.run();
	}
}