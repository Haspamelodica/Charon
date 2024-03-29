package net.haspamelodica.charon.utils.communication;

import static net.haspamelodica.exchanges.sharedmem.SharedMemoryCommon.DEFAULT_BUSY_WAIT_TIMEOUT_NANOS;
import static net.haspamelodica.exchanges.sharedmem.SharedMemoryExchangePool.DEFAULT_BUFSIZE_PER_EXCHANGE_DIRECTION;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import net.haspamelodica.charon.utils.communication.CommunicationParams.SharedFile;

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

		final Optional<SharedFile> sharedfile;
		if(args.consumeIfEqual("shared"))
		{
			Path sharedfilePath = Path.of(args.consume());
			boolean server = Boolean.valueOf(args.consume());
			OptionalInt bufsize;
			if(args.consumeIfEqual("-b") || args.consumeIfEqual("--bufsize"))
				bufsize = OptionalInt.of(args.consumeInteger());
			else
				bufsize = OptionalInt.empty();
			OptionalLong busyWaitTimeoutNanos;
			if(args.consumeIfEqual("-w") || args.consumeIfEqual("--busywait"))
				busyWaitTimeoutNanos = OptionalLong.of(args.consumeLong());
			else
				busyWaitTimeoutNanos = OptionalLong.empty();
			sharedfile = Optional.of(new SharedFile(sharedfilePath, server, bufsize, busyWaitTimeoutNanos));
		} else
			sharedfile = Optional.empty();

		String mode = args.consume();
		return new CommunicationParams(logging, timeout, sharedfile, switch(mode)
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
				[--logging | -l]  [ (--timeout | -t) <timeout> ]
				  	[ shared <sharedfile> <is_server> [ (--bufsize | -b) <bufsize> ] [ (--busywait | -w) <busywait_timeout> ] ]
				  	(
						stdio  |
						listen [<host>] <port>  |
						socket <host> <port>  |
						fifo { in <infile> out <outfile> | out <outfile> in <infile> }  |
						fifos <fifosdir> <controlfifo> <is_server>
					)

				The order of arguments is important.
				-l / --logging:
					Enables logging. Logs will appear on stderr / System.err.
				-t / --timeout:
					<timeout> must an integer >= 0.
					If a non-zero <timeout> is given, waits at most <timeout> millis for communication to initialize.
				sharedfile:
					Communication speed is improved by using the given file for shared memory.
					This file should be in a tmpfs.
					One side (the server side) needs the argument to <is_server> to be true, the other (the client side) needs false.
					This is for a similar reason as the fifo: the input- and output streams must be opened in a certain order.
					<bufsize> is the size of the shared buffer, per exchange direction. It defaults to DEFAULT_BUFSIZE.
					<busywait_timeout> is how long one side should busy-wait for the other at most, in nanoseconds. It defaults to DEFAULT_BUSYWAIT.
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
				"""
				.replace("DEFAULT_BUFSIZE", Integer.toString(DEFAULT_BUFSIZE_PER_EXCHANGE_DIRECTION))
				.replace("DEFAULT_BUSYWAIT", Long.toString(DEFAULT_BUSY_WAIT_TIMEOUT_NANOS));
	}
}
