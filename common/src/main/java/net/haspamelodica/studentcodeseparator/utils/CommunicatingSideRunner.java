package net.haspamelodica.studentcodeseparator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// TODO replace with AutoCloseable wrapper around Input- and OutputStream pair
public class CommunicatingSideRunner<X extends Throwable>
{
	private final CommunicatingSide<X>	action;
	private final CommunicationParams	params;

	private final Semaphore communicationInitialized;

	public static <X extends Throwable> void run(CommunicatingSide<X> action, Class<?> mainClass, String... args)
			throws IOException, InterruptedException, X
	{
		run(action, CommunicationArgsParser.parse(mainClass, args));
	}
	public static <X extends Throwable> void run(CommunicatingSide<X> action, CommunicationParams params)
			throws IOException, InterruptedException, X
	{
		new CommunicatingSideRunner<>(action, params).run();
	}

	private CommunicatingSideRunner(CommunicatingSide<X> action, CommunicationParams params)
	{
		this.action = action;
		this.params = params;

		this.communicationInitialized = params.timeout().isPresent() ? new Semaphore(0) : null;
	}

	private void run() throws IOException, InterruptedException, X
	{
		startTimeoutThread();

		//TODO replace with pattern matching switch once we update to Java 18
		if(params.mode() instanceof CommunicationParams.Mode.Listen mode)
			runListen(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Socket mode)
			runSocket(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Fifo mode)
			runFifo(mode);
		else if(params.mode() instanceof CommunicationParams.Mode.Stdio mode)
			runStdio(mode);
		else
			throw new IllegalArgumentException("Unknown mode: " + params.mode());
	}

	private void runListen(CommunicationParams.Mode.Listen mode) throws IOException, UnknownHostException, X
	{
		try(ServerSocket server = new ServerSocket())
		{
			server.bind(mode.host().isEmpty() ? new InetSocketAddress(mode.port()) : new InetSocketAddress(mode.host().get(), mode.port()));
			try(Socket sock = server.accept())
			{
				run(sock.getInputStream(), sock.getOutputStream());
			}
		}
	}

	private void runSocket(CommunicationParams.Mode.Socket mode) throws IOException, UnknownHostException, X
	{
		try(Socket sock = new Socket(mode.host(), mode.port()))
		{
			run(sock.getInputStream(), sock.getOutputStream());
		}
	}
	private void runFifo(CommunicationParams.Mode.Fifo mode) throws IOException, X
	{
		try(InputStream in = Files.newInputStream(mode.infile()); OutputStream out = Files.newOutputStream(mode.outfile()))
		{
			run(in, out);
		}
	}
	private void runStdio(CommunicationParams.Mode.Stdio mode) throws IOException, X
	{
		run(System.in, System.out);
	}

	private void startTimeoutThread()
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

	private void run(InputStream in, OutputStream out) throws IOException, X
	{
		if(params.timeout().isPresent())
			communicationInitialized.release();
		action.run(in, out, params.logging());
	}

	public static interface CommunicatingSide<X extends Throwable>
	{
		public void run(InputStream in, OutputStream out, boolean logging) throws IOException, X;
	}
}
