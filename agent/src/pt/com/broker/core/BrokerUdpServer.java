package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.IoEventQueueThrottle;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerCodecRouter;
import pt.com.broker.messaging.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.gcs.conf.GcsInfo;

public class BrokerUdpServer
{
	private static Logger log = LoggerFactory.getLogger(BrokerUdpServer.class);

	private int _portNumber;

	private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

	public BrokerUdpServer(int portNumber)
	{
		_portNumber = portNumber;
	}

	public void start()
	{
		try
		{

			ThreadPoolExecutor tpe = new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle(MAX_BUFFER_SIZE));

			final NioDatagramAcceptor acceptor = new NioDatagramAcceptor();

			DefaultIoFilterChainBuilder filterChainBuilder1 = acceptor.getFilterChain();
			filterChainBuilder1.addLast("BROKER_BINARY_CODEC", new ProtocolCodecFilter(BrokerCodecRouter.getInstance()));
			if (GcsInfo.useAccessControl())
			{
				filterChainBuilder1.addLast("AUTHORIZATION_FILTER", AuthorizationFilter.getInstance());
			}
			filterChainBuilder1.addLast("executor", new ExecutorFilter(tpe));

			DatagramSessionConfig dcfg = acceptor.getSessionConfig();
			dcfg.setReuseAddress(true);

			acceptor.setHandler(new BrokerProtocolHandler());

			// Bind
			acceptor.bind(new InetSocketAddress(_portNumber));
			log.info("SAPO-UDP-BROKER Listening on: '{}'.", acceptor.getLocalAddress());
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}
	}
}
