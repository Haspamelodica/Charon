package net.haspamelodica.studentcodeseparator.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

public class CommunicationArgsParser
{
	private final Args args;

	public static CommunicationParams parse(Class<?> mainClass, String... args)
	{
		return new CommunicationArgsParser(mainClass, args).parse();
	}

	private CommunicationArgsParser(Class<?> mainClass, String... args)
	{
		this.args = new Args(usage(mainClass), args);
	}

	private CommunicationParams parse()
	{
		final boolean logging = args.consumeIfEqual("--logging") || args.consumeIfEqual("-l");

		final OptionalInt timeout;
		if(args.consumeIfEqual("--timeout") || args.consumeIfEqual("-t"))
		{
			int timeoutInt = args.consumeInteger();
			if(timeoutInt < 0)
				return args.throwUsage("Timeout must be >= 0");
			timeout = timeoutInt == 0 ? OptionalInt.empty() : OptionalInt.of(timeoutInt);
		} else
			timeout = OptionalInt.empty();

		String mode = args.consume();
		return switch(mode)
		{
			case "listen" -> parseListen(logging, timeout);
			case "socket" -> parseSocket(logging, timeout);
			case "fifo" -> parseFifo(logging, timeout);
			case "stdio" -> parseStdio(logging, timeout);
			default -> args.throwUsage("Unknown mode: " + mode);
		};
	}

	private CommunicationParams parseListen(boolean logging, OptionalInt timeout)
	{
		final Optional<String> host = args.remaining() == 2 ? Optional.of(args.consume()) : Optional.empty();
		final int port = args.consumeInteger();
		args.expectEnd();

		return new CommunicationParams(logging, timeout, new CommunicationParams.Mode.Listen(host, port));
	}

	private CommunicationParams parseSocket(boolean logging, OptionalInt timeout)
	{
		String host = args.consume();
		int port = args.consumeInteger();
		args.expectEnd();

		return new CommunicationParams(logging, timeout, new CommunicationParams.Mode.Socket(host, port));
	}
	private CommunicationParams parseFifo(boolean logging, OptionalInt timeout)
	{
		String firstGivenFifoDirection = args.consume();
		return switch(firstGivenFifoDirection)
		{
			case "in" ->
			{
				Path infile = Path.of(args.consume());
				args.expect("out");
				Path outfile = Path.of(args.consume());
				args.expectEnd();

				yield new CommunicationParams(logging, timeout, new CommunicationParams.Mode.Fifo(infile, outfile));
			}
			case "out" ->
			{
				Path outfile = Path.of(args.consume());
				args.expect("in");
				Path infile = Path.of(args.consume());
				args.expectEnd();

				yield new CommunicationParams(logging, timeout, new CommunicationParams.Mode.Fifo(infile, outfile));
			}
			default -> args.throwUsage("Unknown fifo direction: " + firstGivenFifoDirection);
		};
	}
	private CommunicationParams parseStdio(boolean logging, OptionalInt timeout)
	{
		args.expectEnd();

		return new CommunicationParams(logging, timeout, new CommunicationParams.Mode.Stdio());
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

	public static interface CommunicatingSide<X extends Throwable>
	{
		public void run(InputStream in, OutputStream out, boolean logging) throws IOException, X;
	}
}
