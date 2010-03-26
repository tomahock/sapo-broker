package pt.com.broker.http;

import java.util.concurrent.Executor;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrokerServer is responsible for initializing client's HTTP interface (MINA infrastructure).
 */

public class BrokerHttpService
{
	private static final Logger LOG = LoggerFactory.getLogger(BrokerHttpService.class);

	private int _portNumber;

	private final Executor tpeIo;
	private final Executor tpeWorkers;

	public BrokerHttpService(Executor tpe_io, Executor tpe_workers, int portNumber)
	{
		tpeIo = tpe_io;
		tpeWorkers = tpe_workers;
		_portNumber = portNumber;
	}

	public void start()
	{
		try
		{
			NettyHttpServer server = new NettyHttpServer(null, false, tpeIo, tpeWorkers);
			server.setPort(_portNumber);
			server.setRouter(new BrokerRequestRouter());
			server.start();
		}
		catch (Throwable ex)
		{
			Throwable rootCause = ErrorAnalyser.findRootCause(ex);
			LOG.error("Failed to start HTTP container!", rootCause);
			Shutdown.now();
		}
	}
}