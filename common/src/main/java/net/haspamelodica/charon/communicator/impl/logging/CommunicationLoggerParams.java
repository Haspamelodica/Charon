package net.haspamelodica.charon.communicator.impl.logging;

public record CommunicationLoggerParams(String prefix, boolean useRefToString, boolean useObjectToString)
{
	public static final String DEFAULT_PREFIX = "";
	public static final CommunicationLoggerParams DEFAULT = new CommunicationLoggerParams(DEFAULT_PREFIX, false, false);
	public static final CommunicationLoggerParams DEFAULT_REF_TO_STRING = new CommunicationLoggerParams(DEFAULT_PREFIX, true, false);
	public static final CommunicationLoggerParams DEFAULT_ALL_TO_STRING = new CommunicationLoggerParams(DEFAULT_PREFIX, true, true);
}