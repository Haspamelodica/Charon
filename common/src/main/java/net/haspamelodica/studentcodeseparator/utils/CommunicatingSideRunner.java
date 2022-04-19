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

public class CommunicatingSideRunner
{
	private final CommunicatingSide action;

	private final Args args;

	private boolean logging;

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
		logging = args.consumeIfEqual("-l") || args.consumeIfEqual("--logging");

		switch(args.consume())
		{
			case "listen" -> runListen();
			case "socket" -> runSocket();
			case "fifo" -> runFifo();
			case "stdio" -> runStdio();
			default -> args.throwUsage();
		}
	}

	private void runListen() throws IOException, UnknownHostException
	{
		String host = args.remaining() == 2 ? args.consume() : null;
		int port = args.consumeInteger();
		args.expectEnd();

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

		try(Socket sock = new Socket(host, port))
		{
			run(sock.getInputStream(), sock.getOutputStream());
		}
	}
	private void runFifo() throws IOException
	{
		String infile = args.consume();
		String outfile = args.consume();
		args.expectEnd();

		try(InputStream in = Files.newInputStream(Path.of((infile))); OutputStream out = Files.newOutputStream(Path.of(outfile)))
		{
			run(in, out);
		}
	}
	private void runStdio() throws IOException
	{
		args.expectEnd();

		run(System.in, System.out);
	}

	private void run(InputStream in, OutputStream out) throws IOException
	{
		action.run(in, out, logging);
	}

	private static String usage(Class<?> mainClass)
	{
		return """
				Usage:
					INVOKE [--logging | -l] {
						listen [<host>] <port>  |
						socket <host> <port>  |
						fifo <infile> <outfile>  |
						stdio  }

				-l / --logging:
					Enables logging. Logs will appear on stderr / System.err.
				listen:
					Listens for one incoming socket connection on <port> on interface <host>, or on all interfaces if no host is given.
				socket:
					Connects to a socket on the given host and port.
				fifo:
					Reads input from file <infile> and writes output to file <outfile>. Meant to be used with *nix fifos.
				stdio:
					Reads input from stdin / System.in and writes output to stdout / System.out.
					Does not (yet) work if any other part of the program uses stdin/out.
				"""
				.replace("INVOKE", "java " + mainClass.getName());
	}

	//TODO Der Name ist Programm
	public static interface CommunicatingSide
	{
		public void run(InputStream in, OutputStream out, boolean logging) throws IOException;
	}
}
