package net.haspamelodica.studentcodeseparator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CommunicatingSideRunner
{
	private final CommunicatingSide action;

	private final Args args;

	private boolean		logging;
	private int			timeout;
	private Semaphore	communicationInitialized;

	public static void run(CommunicatingSide action, Class<?> mainClass, String... args)
			throws IOException, InterruptedException
	{
		new CommunicatingSideRunner(action, mainClass, args).run();
	}

	private CommunicatingSideRunner(CommunicatingSide action, Class<?> mainClass, String... args)
	{
		this.action = action;
		this.args = new Args(usage(mainClass), args);
	}

	private void run() throws IOException, InterruptedException
	{
		logging = args.consumeIfEqual("--logging") || args.consumeIfEqual("-l");

		if(args.consumeIfEqual("--timeout") || args.consumeIfEqual("-t"))
		{
			timeout = args.consumeInteger();
			if(timeout < 0)
				args.throwUsage("Timeout must be >= 0");
			communicationInitialized = new Semaphore(0);
		}

		String mode = args.consume();
		switch(mode)
		{
			case "listen" -> runListen();
			case "socket" -> runSocket();
			case "fifo" -> runFifo();
			case "stdio" -> runStdio();
			default -> args.throwUsage("Unknown mode: " + mode);
		}
	}

	private void runListen() throws IOException, UnknownHostException
	{
		String host = args.remaining() == 2 ? args.consume() : null;
		int port = args.consumeInteger();
		args.expectEnd();

		startTimeoutThread();
		try(ServerSocket server = new ServerSocket())
		{
			server.bind(host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port));
			try(Socket sock = server.accept())
			{
				run(sock.getInputStream(), sock.getOutputStream());
			}
		}
	}

	private void runSocket() throws IOException, UnknownHostException
	{
		String host = args.consume();
		int port = args.consumeInteger();
		args.expectEnd();

		startTimeoutThread();
		try(Socket sock = new Socket(host, port))
		{
			run(sock.getInputStream(), sock.getOutputStream());
		}
	}
	private void runFifo() throws IOException
	{
		String firstGivenFifoDirection = args.consume();
		switch(firstGivenFifoDirection)
		{
			case "in" ->
			{
				String infile = args.consume();
				args.expect("out");
				String outfile = args.consume();
				args.expectEnd();

				startTimeoutThread();
				try(InputStream in = Files.newInputStream(Path.of(infile)); OutputStream out = Files.newOutputStream(Path.of(outfile)))
				{
					run(in, out);
				}
			}
			case "out" ->
			{
				String outfile = args.consume();
				args.expect("in");
				String infile = args.consume();
				args.expectEnd();

				startTimeoutThread();
				try(OutputStream out = Files.newOutputStream(Path.of(outfile)); InputStream in = Files.newInputStream(Path.of(infile)))
				{
					run(in, out);
				}
			}
			default -> args.throwUsage("Unknown fifo direction: " + firstGivenFifoDirection);
		}
	}
	private void runStdio() throws IOException
	{
		args.expectEnd();

		run(System.in, System.out);
	}

	private void startTimeoutThread()
	{
		if(timeout == 0)
			return;

		Thread timeoutThread = new Thread(() ->
		{
			boolean communicationInitSuccessful;
			try
			{
				communicationInitSuccessful = communicationInitialized.tryAcquire(timeout, TimeUnit.MILLISECONDS);
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

	private void run(InputStream in, OutputStream out) throws IOException
	{
		if(timeout > 0)
			communicationInitialized.release();
		action.run(in, out, logging);
	}

	private static String usage(Class<?> mainClass)
	{
		return """
				Usage:
					INVOKE  [--logging | -l]  { [--timeout | -t ] <timeout> }  {
						listen [<host>] <port>  |
						socket <host> <port>  |
						fifo { in <infile> out <outfile> | out <outfile> in <infile> }  |
						stdio
					}

				-l / --logging:
					Enables logging. Logs will appear on stderr / System.err.
				-t / --timeout:
					<timeout> must an integer >= 0.
					If a non-zero <timeout> is given, waits at most <timeout> millis for communication to initialize.
					If the initialization times out, the entire JVM is terminated with exit code 1.
				listen:
					Listens for one incoming socket connection on <port> on interface <host>, or on all interfaces if no host is given.
				socket:
					Connects to a socket on the given host and port.
				fifo:
					Reads input from file <infile> and writes output to file <outfile>. Meant to be used with *nix fifos.
					The fifo given first will be opened first. This is important because
					opening a *nix fifo only finishes when the other end opens the fifo as well.
				stdio:
					Reads input from stdin / System.in and writes output to stdout / System.out.
					Does not (yet) work if any other part of the program uses stdin/out.
				"""
				.replace("INVOKE", "java " + mainClass.getName());
	}

	public static interface CommunicatingSide
	{
		public void run(InputStream in, OutputStream out, boolean logging) throws IOException;
	}
}
