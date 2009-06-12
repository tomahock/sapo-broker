package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.IoEventQueueThrottle;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerCodecRouter;
import pt.com.broker.codec.xml.SoapCodec;
import pt.com.broker.messaging.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.gcs.conf.GcsInfo;

/**
 * BrokerServer is responsible for initializing client's TCP interface (MINA infrastructure).
 *
 */

public class BrokerServer
{
	private static Logger log = LoggerFactory.getLogger(BrokerServer.class);

	private int _portNumber;

	private int _legacyPortNumber;

	private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	public BrokerServer(int portNumber, int legacyPortNumber)
	{
		_portNumber = portNumber;
		_legacyPortNumber = legacyPortNumber;
	}

	public void start()
	{
		try
		{
			ThreadPoolExecutor tpe = new OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new IoEventQueueThrottle(MAX_BUFFER_SIZE));
			final SocketAcceptor acceptor0 = new NioSocketAcceptor(NCPU);

			acceptor0.setReuseAddress(true);
			((SocketSessionConfig) acceptor0.getSessionConfig()).setReuseAddress(true);
			((SocketSessionConfig) acceptor0.getSessionConfig()).setTcpNoDelay(false);
			((SocketSessionConfig) acceptor0.getSessionConfig()).setKeepAlive(true);

			AuthorizationFilter authFilter = AuthorizationFilter.getInstance();

			DefaultIoFilterChainBuilder filterChainBuilder0 = acceptor0.getFilterChain();
			filterChainBuilder0.addLast("BROKER_CODEC", new ProtocolCodecFilter(new SoapCodec(GcsInfo.getMessageMaxSize())));
			if (GcsInfo.useAccessControl())
			{
				filterChainBuilder0.addLast("AUTHORIZATION_FILTER", authFilter);
			}
			filterChainBuilder0.addLast("executor", new ExecutorFilter(tpe));

			acceptor0.setHandler(new BrokerProtocolHandler());

			// Bind
			acceptor0.bind(new InetSocketAddress(_legacyPortNumber));
			log.info("SAPO-BROKER (legacy protocol)  Listening on: '{}'.", acceptor0.getLocalAddress());

			final SocketAcceptor acceptor1 = new NioSocketAcceptor(NCPU);

			acceptor1.setReuseAddress(true);
			((SocketSessionConfig) acceptor1.getSessionConfig()).setReuseAddress(true);
			((SocketSessionConfig) acceptor1.getSessionConfig()).setTcpNoDelay(false);
			((SocketSessionConfig) acceptor1.getSessionConfig()).setKeepAlive(true);

			DefaultIoFilterChainBuilder filterChainBuilder1 = acceptor1.getFilterChain();
			filterChainBuilder1.addLast("BROKER_BINARY_CODEC", new ProtocolCodecFilter(BrokerCodecRouter.getInstance()));
			if (GcsInfo.useAccessControl())
			{
				filterChainBuilder1.addLast("AUTHORIZATION_FILTER", authFilter);
			}
			filterChainBuilder1.addLast("executor", new ExecutorFilter(tpe));

			acceptor1.setHandler(new BrokerProtocolHandler());

			// Bind
			acceptor1.bind(new InetSocketAddress(_portNumber));
			log.info("SAPO-BROKER Listening on: '{}'.", acceptor1.getLocalAddress());

		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}
	}

}
