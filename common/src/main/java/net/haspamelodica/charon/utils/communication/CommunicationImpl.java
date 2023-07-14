package net.haspamelodica.charon.utils.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.haspamelodica.exchanges.Exchange;
import net.haspamelodica.exchanges.ExchangePool;
import net.haspamelodica.exchanges.FifosExchangePoolClient;
import net.haspamelodica.exchanges.FifosExchangePoolServer;
import net.haspamelodica.exchanges.multiplexed.MultiplexedExchangePool;

public class CommunicationImpl implements Communication
{
	private final boolean		logging;
	private final ExchangePool	exchangePool;

	private final Semaphore communicationInitialized;

	public CommunicationImpl(CommunicationParams params) throws IOException, InterruptedException
	{
		this.logging = params.logging();
		this.communicationInitialized = params.timeout().isPresent() ? new Semaphore(0) : null;

		startTimeoutThread(params);
		this.exchangePool = openCommunication(params);
		stopTimeout();
	}

	private static ExchangePool openCommunication(CommunicationParams params) throws IOException, InterruptedException
	{
		//TODO replace with pattern matching switch once we update to Java 18
		if(params.mode() instanceof CommunicationParams.Mode.Stdio mode)
			return openMultiplexed(openStdio(mode));
		else if(params.mode() instanceof CommunicationParams.Mode.Listen mode)
			return openMultiplexed(openListen(mode));
		else if(params.mode() instanceof CommunicationParams.Mode.Socket mode)
			return openMultiplexed(openSocket(mode));
		else if(params.mode() instanceof CommunicationParams.Mode.Fifo mode)
			return openMultiplexed(openFifo(mode));
		else if(params.mode() instanceof CommunicationParams.Mode.Fifos mode)
			return openFifos(mode);
		else
			throw new IllegalArgumentException("Unknown mode: " + params.mode());
	}

	private static ExchangePool openMultiplexed(Exchange exchange)
	{
		//TODO make wrapBuffered configurable
		return new MultiplexedExchangePool(exchange.wrapBuffered());
	}

	private static Exchange openStdio(CommunicationParams.Mode.Stdio mode) throws IOException
	{
		return new Exchange(System.in, System.out);
	}

	private static Exchange openListen(CommunicationParams.Mode.Listen mode) throws IOException
	{
		// yes, open ServerSocket in try-with-resource: close it after accept succeeded; accepted connection will live on.
		try(ServerSocket server = new ServerSocket())
		{
			server.bind(mode.host().isEmpty()
					? new InetSocketAddress(mode.port())
					: new InetSocketAddress(mode.host().get(), mode.port()));
			Socket sock = server.accept();

			return new Exchange(sock.getInputStream(), sock.getOutputStream());
		}
	}

	private static Exchange openSocket(CommunicationParams.Mode.Socket mode) throws IOException
	{
		Socket sock = new Socket(mode.host(), mode.port());

		return new Exchange(sock.getInputStream(), sock.getOutputStream());
	}
	private static Exchange openFifo(CommunicationParams.Mode.Fifo mode) throws IOException
	{
		return Exchange.openFifos(mode.inFirst(), mode.infile(), mode.outfile());
	}
	private static ExchangePool openFifos(CommunicationParams.Mode.Fifos mode) throws IOException
	{
		return mode.server()
				? new FifosExchangePoolServer(mode.fifosdir(), mode.controlfifo())
				: new FifosExchangePoolClient(mode.fifosdir(), mode.controlfifo());
	}

	private void startTimeoutThread(CommunicationParams params)
	{
		if(params.timeout().isEmpty())
			return;

		Thread timeoutThread = new Thread(() ->
		{
			boolean communicationInitSuccessful;
			try
			{
				communicationInitSuccessful = communicationInitialized.tryAcquire(params.timeout().getAsInt(), TimeUnit.MILLISECONDS);
			} catch(InterruptedException e)
			{
				//ignore; don't timeout anymore
				communicationInitSuccessful = true;
			}
			if(!communicationInitSuccessful)
			{
				System.err.println("Opening communication timed out");
				System.err.flush();
				//TODO don't use System.exit. Is interrupting the other thread enough?
				System.exit(1);
			}
		});
		timeoutThread.setDaemon(true);
		timeoutThread.start();
	}

	private void stopTimeout()
	{
		if(communicationInitialized != null)
			communicationInitialized.release();
	}

	@Override
	public boolean getLogging()
	{
		return logging;
	}
	@Override
	public ExchangePool getExchangePool()
	{
		return exchangePool;
	}

	@Override
	public void close() throws IOException
	{
		exchangePool.close();
	}
}
