package net.haspamelodica.charon.utils.communication;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

public class CommunicationArgsParser
{
	private final Args args;

	public static CommunicationParams parseSpaceSeparated(String s) throws IncorrectUsageException
	{
		return parse(s.split(" "));
	}
	public static CommunicationParams parse(String... args) throws IncorrectUsageException
	{
		return new CommunicationArgsParser(args).parse();
	}

	private CommunicationArgsParser(String... args)
	{
		this.args = new Args(args);
	}

	private CommunicationParams parse() throws IncorrectUsageException
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
		return new CommunicationParams(logging, timeout, switch(mode)
		{
			case "stdio" -> parseStdio();
			case "listen" -> parseListen();
			case "socket" -> parseSocket();
			case "fifo" -> parseFifo();
			case "fifos" -> parseFifos();
			default -> args.throwUsage("Unknown mode: " + mode);
		});
	}

	private CommunicationParams.Mode parseStdio() throws IncorrectUsageException
	{
		args.expectEnd();

		return new CommunicationParams.Mode.Stdio();
	}
	private CommunicationParams.Mode parseListen() throws IncorrectUsageException
	{
		final Optional<String> host = args.remaining() == 2 ? Optional.of(args.consume()) : Optional.empty();
		final int port = args.consumeInteger();
		args.expectEnd();

		return new CommunicationParams.Mode.Listen(host, port);
	}

	private CommunicationParams.Mode parseSocket() throws IncorrectUsageException
	{
		String host = args.consume();
		int port = args.consumeInteger();
		args.expectEnd();

		return new CommunicationParams.Mode.Socket(host, port);
	}

	private CommunicationParams.Mode parseFifo() throws IncorrectUsageException
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

				yield new CommunicationParams.Mode.Fifo(true, infile, outfile);
			}
			case "out" ->
			{
				Path outfile = Path.of(args.consume());
				args.expect("in");
				Path infile = Path.of(args.consume());
				args.expectEnd();

				yield new CommunicationParams.Mode.Fifo(false, infile, outfile);
			}
			default -> args.throwUsage("Unknown fifo direction: " + firstGivenFifoDirection);
		};
	}

	private CommunicationParams.Mode parseFifos() throws IncorrectUsageException
	{
		Path fifosdir = Path.of(args.consume());
		Path controlfifo = Path.of(args.consume());
		boolean server = Boolean.valueOf(args.consume());
		args.expectEnd();

		return new CommunicationParams.Mode.Fifos(fifosdir, controlfifo, server);
	}

	public static String argsSyntax()
	{
		return """
				[--logging | -l]  { [--timeout | -t ] <timeout> }  {
						stdio  |
						listen [<host>] <port>  |
						socket <host> <port>  |
						fifo { in <infile> out <outfile> | out <outfile> in <infile> }  |
						fifos <fifosdir> <controlfifo> <is_server>
					}

				-l / --logging:
					Enables logging. Logs will appear on stderr / System.err.
				-t / --timeout:
					<timeout> must an integer >= 0.
					If a non-zero <timeout> is given, waits at most <timeout> millis for communication to initialize.
				stdio:
					Communication is multiplexed over stdio:
					Reads input from stdin / System.in and writes output to stdout / System.out.
					Does not (yet) work if any other part of the program uses stdin/out.
				listen:
					Communication is multiplexed over one socket:
					Listens for one incoming socket connection on <port> on interface <host>, or on all interfaces if no host is given.
				socket:
					Communication is multiplexed over one socket:
					Connects to a socket on the given host and port.
				fifo:
					Communication is multiplexed over one pair of fifos:
					Reads input from file <infile> and writes output to file <outfile>. Meant to be used with *nix fifos.
					The fifo given first will be opened first. This is important because
					opening a *nix fifo only finishes when the other end opens the fifo as well.
				fifos:
					Communicates using multiple *nix fifos, which are dynamically created when neccessary.
					Dynamically created fifos will be created in <fifosdir>.
					Control information will be exchanged over <controlfifo>.
					One side (the server side) needs the argument to <is_server> to be true, the other (the client side) needs false.
					The server side must be able to create fifos in <fifosdir>, the client side must only be able to read them.
				""";
	}
}
