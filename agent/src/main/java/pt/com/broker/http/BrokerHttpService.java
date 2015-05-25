package pt.com.broker.http;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.caudexorigo.http.netty4.NettyHttpServer;
import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrokerServer is responsible for initializing client's HTTP interface
 */

public class BrokerHttpService
{
	private static final Logger LOG = LoggerFactory.getLogger(BrokerHttpService.class);

	private final int _portNumber;

	private final NettyContext nettyCtx;

	public BrokerHttpService(int portNumber, NettyContext nettyCtx)
	{
		_portNumber = portNumber;
		this.nettyCtx = nettyCtx;
	}

	public void start()
	{
		try
		{
			nettyCtx.getWorkerEventLoopGroup().execute(new Runnable()
			{
				@Override
				public void run()
				{

					/* TODO TEMP CHANGE brsantos */
					NettyHttpServer server = new NettyHttpServer("0.0.0.0", _portNumber);
					server.setNettyContext(nettyCtx);

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