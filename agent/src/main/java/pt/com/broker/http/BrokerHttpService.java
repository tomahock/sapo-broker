package pt.com.broker.http;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty4.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrokerServer is responsible for initializing client's HTTP interface (MINA infrastructure).
 */

public class BrokerHttpService
{
	private static final Logger LOG = LoggerFactory.getLogger(BrokerHttpService.class);

	private final int _portNumber;

	private final Executor tpeIo;
	private final Executor tpeWorkers;

    final Executor executor = Executors.newSingleThreadScheduledExecutor();

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

            executor.execute(new Runnable() {
                @Override
                public void run() {

                    /* TODO TEMP CHANGE brsantos */
                    NettyHttpServer server = new NettyHttpServer("0.0.0.0", _portNumber, false);


                    /* TEMP CHANGE brsantos */
                    server.setRouter(new BrokerRequestRouter());

                    server.start();

                }
            });

		}
		catch (Throwable ex)
		{
			Throwable rootCause = ErrorAnalyser.findRootCause(ex);
			LOG.error("Failed to start HTTP container!", rootCause);
			Shutdown.now();
		}
	}
}