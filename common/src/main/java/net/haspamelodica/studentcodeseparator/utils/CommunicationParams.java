package net.haspamelodica.studentcodeseparator.utils;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;

import net.haspamelodica.studentcodeseparator.utils.CommunicationParams.Mode.Fifo;
import net.haspamelodica.studentcodeseparator.utils.CommunicationParams.Mode.Listen;
import net.haspamelodica.studentcodeseparator.utils.CommunicationParams.Mode.Socket;
import net.haspamelodica.studentcodeseparator.utils.CommunicationParams.Mode.Stdio;

public record CommunicationParams(boolean logging, OptionalInt timeout, CommunicationParams.Mode mode)
{
	public static sealed interface Mode permits Listen, Socket, Fifo, Stdio
	{
		public static record Listen(Optional<String> host, int port) implements Mode
		{}
		public static record Socket(String host, int port) implements Mode
		{}
		public static record Fifo(Path infile, Path outfile) implements Mode
		{}
		public static record Stdio() implements Mode
		{}
	}
}
