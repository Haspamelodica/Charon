package net.haspamelodica.studentcodeseparator;

import static net.haspamelodica.studentcodeseparator.communicator.impl.LoggingCommunicatorServerSide.maybeWrapLoggingS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.haspamelodica.studentcodeseparator.communicator.impl.data.student.DataCommunicatorServer;
import net.haspamelodica.studentcodeseparator.communicator.impl.samejvm.DirectSameJVMCommunicatorServerSide;
import net.haspamelodica.studentcodeseparator.refs.Ref;
import net.haspamelodica.studentcodeseparator.refs.direct.WeakDirectRefManager;
import net.haspamelodica.studentcodeseparator.refs.intref.owner.IDReferrer;
import net.haspamelodica.studentcodeseparator.utils.communication.Communication;
import net.haspamelodica.studentcodeseparator.utils.communication.CommunicationArgsParser;
import net.haspamelodica.studentcodeseparator.utils.communication.IncorrectUsageException;

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
		DataCommunicatorServer<?> server = new DataCommunicatorServer<>(in, out,
				maybeWrapLoggingS(new DirectSameJVMCommunicatorServerSide<>(new WeakDirectRefManager<Ref<Object, IDReferrer>>()), logging));
		server.run();
	}
}