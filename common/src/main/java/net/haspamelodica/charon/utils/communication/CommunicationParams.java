package net.haspamelodica.charon.utils.communication;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import net.haspamelodica.charon.utils.communication.CommunicationParams.Mode.Fifo;
import net.haspamelodica.charon.utils.communication.CommunicationParams.Mode.Fifos;
import net.haspamelodica.charon.utils.communication.CommunicationParams.Mode.Listen;
import net.haspamelodica.charon.utils.communication.CommunicationParams.Mode.Socket;
import net.haspamelodica.charon.utils.communication.CommunicationParams.Mode.Stdio;

public record CommunicationParams(boolean logging, OptionalInt timeout,
		Optional<CommunicationParams.SharedFile> sharedfile, CommunicationParams.Mode mode)
{
	public static record SharedFile(Path sharedfile, boolean server, OptionalInt bufsize, OptionalLong busyWaitTimeoutNanos)
	{}
	public static sealed interface Mode permits Stdio, Listen, Socket, Fifo, Fifos
	{
		public static record Stdio() implements Mode
		{}
		public static record Listen(Optional<String> host, int port) implements Mode
		{}
		public static record Socket(String host, int port) implements Mode
		{}
		public static record Fifo(boolean inFirst, Path infile, Path outfile) implements Mode
		{}
		public static record Fifos(Path fifosdir, Path controlfifo, boolean server) implements Mode
		{}
	}
}
