package net.haspamelodica.charon.communicator.impl.logging;

public record CommunicationLoggerParams(String prefix)
{
	public static final String DEFAULT_PREFIX = "";
	public static final CommunicationLoggerParams DEFAULT = new CommunicationLoggerParams(DEFAULT_PREFIX);
}