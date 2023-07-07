package net.haspamelodica.charon.utils.communication;

import java.io.IOException;

import net.haspamelodica.exchanges.ExchangePool;

public interface Communication extends AutoCloseable
{
	public boolean getLogging();
	public ExchangePool getExchangePool();

	@Override
	public void close() throws IOException;

	public static Communication open(CommunicationParams params) throws IOException, InterruptedException
	{
		return new CommunicationImpl(params);
	}
}
