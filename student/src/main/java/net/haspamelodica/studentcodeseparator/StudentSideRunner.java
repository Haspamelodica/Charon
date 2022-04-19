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
import net.haspamelodica.studentcodeseparator.utils.CommunicatingSideRunner;

public class StudentSideRunner
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		CommunicatingSideRunner.run(StudentSideRunner::run, StudentSideRunner.class, args);
	}

	public static void run(InputStream in, OutputStream out, boolean logging) throws IOException
	{
		DataCommunicatorServer<?> server = new DataCommunicatorServer<>(in, out,
				maybeWrapLoggingS(new DirectSameJVMCommunicatorServerSide<>(new WeakDirectRefManager<Ref<Object, IDReferrer>>()), logging));
		server.run();
	}
}
