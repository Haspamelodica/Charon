package net.haspamelodica.charon.utils.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.haspamelodica.charon.utils.communication.CommunicationParams.SharedFile;
import net.haspamelodica.exchanges.Exchange;
import net.haspamelodica.exchanges.ExchangePool;
import net.haspamelodica.exchanges.fifos.FifosExchangePoolClient;
import net.haspamelodica.exchanges.fifos.FifosExchangePoolServer;
import net.haspamelodica.exchanges.multiplexed.MultiplexedExchangePool;
import net.haspamelodica.exchanges.sharedmem.SharedMemoryExchangePool;

public class CommunicationImpl implements Communication
{
	//TODO make this configurable
	private final static boolean EXCHANGE_STATS = false;

	private final boolean		logging;
	private final ExchangePool	exchangePool;

	private final Semaphore communicationInitialized;

	public CommunicationImpl(CommunicationParams params) throws IOException, InterruptedException
	{
		this.logging = params.logging();
		this.communicationInitialized = params.timeout().isPresent() ? new Semaphore(0) : null;

		startTimeoutThread(params);
		ExchangePool unwrappedExchangePool = openCommunication(params.sharedfile(), params.mode());
		this.exchangePool = EXCHANGE_STATS ? unwrappedExchangePool.wrapStatistics(System.err) : unwrappedExchangePool;
		stopTimeout();
	}

	private ExchangePool openCommunication(Optional<SharedFile> sharedfileOpt, CommunicationParams.Mode mode)
			throws IOException, InterruptedException
	{
		ExchangePool justMode = openCommunicationJustMode(mode);
		if(sharedfileOpt.isEmpty())
			return justMode;

		SharedFile sharedfile = sharedfileOpt.get();
		return new SharedMemoryExchangePool(justMode, sharedfile.sharedfile(), sharedfile.server());
	}

	private ExchangePool openCommunicationJustMode(CommunicationParams.Mode mode) throws IOException, InterruptedException
	{
		//TODO replace with pattern matching switch once we update to Java 18
		if(mode instanceof CommunicationParams.Mode.Stdio modeCasted)
			return openMultiplexed(openStdio(modeCasted));
		else if(mode instanceof CommunicationParams.Mode.Listen modeCasted)
			return openMultiplexed(openListen(modeCasted));
		else if(mode instanceof CommunicationParams.Mode.Socket modeCasted)
			return openMultiplexed(openSocket(modeCasted));
		else if(mode instanceof CommunicationParams.Mode.Fifo modeCasted)
			return openMultiplexed(openFifo(modeCasted));
		else if(mode instanceof CommunicationParams.Mode.Fifos modeCasted)
			return openFifos(modeCasted);
		else
			throw new IllegalArgumentException("Unknown mode: " + mode);
	}

	private ExchangePool openMultiplexed(Exchange exchange)
	{
		//TODO make wrapBuffered configurable
		Exchange wrappedExchange = EXCHANGE_STATS
				? exchange.wrapStatistics(System.err, "ru").wrapBuffered().wrapStatistics(System.err, "rb")
				: exchange.wrapBuffered();
		return new MultiplexedExchangePool(wrappedExchange);
	}

	private static Exchange openStdio(CommunicationParams.Mode.Stdio mode) throws IOException
	{
		return Exchange.ofNoExtraCloseAction(System.in, System.out);
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
			sock.setTcpNoDelay(true);

			return Exchange.of(sock.getInputStream(), sock.getOutputStream(), sock::close);
		}
	}

	private static Exchange openSocket(CommunicationParams.Mode.Socket mode) throws IOException
	{
		Socket sock = new Socket(mode.host(), mode.port());
		sock.setTcpNoDelay(true);

		return Exchange.of(sock.getInputStream(), sock.getOutputStream(), sock::close);
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
