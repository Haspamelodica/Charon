package net.haspamelodica.studentcodeseparator.utils.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CommunicationImpl implements Communication
{
	private final boolean		logging;
	private final InputStream	in;
	private final OutputStream	out;

	private final Semaphore communicationInitialized;

	public CommunicationImpl(CommunicationParams params) throws IOException, InterruptedException
	{
		this.logging = params.logging();
		this.communicationInitialized = params.timeout().isPresent() ? new Semaphore(0) : null;

		startTimeoutThread(params);
		InputOutputStreamPair inoutPair = openCommunication(params);
		stopTimeout();

		this.in = inoutPair.in();
		this.out = inoutPair.out();
	}

	private InputOutputStreamPair openCommunication(CommunicationParams params) throws IOException, InterruptedException
	{
		//TODO replace with pattern matching switch once we update to Java 18
		if(params.mode() instanceof CommunicationParams.Mode.Listen mode)
			return openListen(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Socket mode)
			return openSocket(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Fifo mode)
			return openFifo(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Stdio mode)
			return openStdio(mode);
		else
			throw new IllegalArgumentException("Unknown mode: " + params.mode());
	}

	private InputOutputStreamPair openListen(CommunicationParams.Mode.Listen mode) throws IOException
	{
		// yes, open ServerSocket in try-with-resource: close it after accept succeeded; accepted connection will live on.
		try(ServerSocket server = new ServerSocket())
		{
			server.bind(mode.host().isEmpty()
					? new InetSocketAddress(mode.port())
					: new InetSocketAddress(mode.host().get(), mode.port()));
			Socket sock = server.accept();

			return new InputOutputStreamPair(sock.getInputStream(), sock.getOutputStream());
		}
	}

	private InputOutputStreamPair openSocket(CommunicationParams.Mode.Socket mode) throws IOException
	{
		Socket sock = new Socket(mode.host(), mode.port());

		return new InputOutputStreamPair(sock.getInputStream(), sock.getOutputStream());
	}
	private InputOutputStreamPair openFifo(CommunicationParams.Mode.Fifo mode) throws IOException
	{
		InputStream in;
		OutputStream out;
		if(mode.inFirst())
		{
			in = Files.newInputStream(mode.infile());
			out = Files.newOutputStream(mode.outfile());
		} else
		{
			out = Files.newOutputStream(mode.outfile());
			in = Files.newInputStream(mode.infile());
		}

		return new InputOutputStreamPair(in, out);
	}
	private InputOutputStreamPair openStdio(CommunicationParams.Mode.Stdio mode) throws IOException
	{
		return new InputOutputStreamPair(System.in, System.out);
	}

	private void startTimeoutThread(CommunicationParams params)
	{
		if(params.timeout().isEmpty())
			return;

		Thread timeoutThread = new Thread(() ->
		{
			boolean communicationInitSuccessful;
			try
			{
				communicationInitSuccessful = communicationInitialized.tryAcquire(params.timeout().getAsInt(), TimeUnit.MILLISECONDS);
			} catch(InterruptedException e)
			{
				//ignore; don't timeout anymore
				communicationInitSuccessful = true;
			}
			if(!communicationInitSuccessful)
			{
				System.err.println("Opening communication timed out");
				System.err.flush();
				//TODO don't use System.exit. Is interrupting the other thread enough?
				System.exit(1);
			}
		});
		timeoutThread.setDaemon(true);
		timeoutThread.start();
	}

	private void stopTimeout()
	{
		if(communicationInitialized != null)
			communicationInitialized.release();
	}

	@Override
	public boolean getLogging()
	{
		return logging;
	}
	@Override
	public InputStream getIn()
	{
		return in;
	}
	@Override
	public OutputStream getOut()
	{
		return out;
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			in.close();
		} finally
		{
			out.close();
		}
	}

	private static final record InputOutputStreamPair(InputStream in, OutputStream out)
	{}
}
