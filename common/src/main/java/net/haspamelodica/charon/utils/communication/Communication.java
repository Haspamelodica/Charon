package net.haspamelodica.charon.utils.communication;

import java.io.IOException;

import net.haspamelodica.exchanges.Exchange;

public interface Communication extends AutoCloseable
{
	public boolean getLogging();
	public Exchange getExchange();

	@Override
	public void close() throws IOException;

	public static Communication open(CommunicationParams params) throws IOException, InterruptedException
	{
		return new CommunicationImpl(params);
	}
}
