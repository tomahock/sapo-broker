package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerSslPipelineFactory;

/**
 * BrokerServer is responsible for initializing client's SSL interface (MINA infrastructure).
 */

public class BrokerSSLServer
{

	private static Logger log = LoggerFactory.getLogger(BrokerSSLServer.class);

	private int _portNumber;

	private final Executor tpeIo;
	private final Executor tpeWorkers;

	public BrokerSSLServer(Executor tpe_io, Executor tpe_workers, int portNumber)
	{
		tpeIo = tpe_io;
		tpeWorkers = tpe_workers;
		_portNumber = portNumber;
	}

	public void start()
	{
		try
		{
			ChannelFactory factory = new NioServerSocketChannelFactory(tpeIo, tpeWorkers);
			ServerBootstrap bootstrap = new ServerBootstrap(factory);

			bootstrap.setOption("child.tcpNoDelay", true);
			bootstrap.setOption("child.keepAlive", true);
			bootstrap.setOption("child.receiveBufferSize", 128 * 1024);
			bootstrap.setOption("child.sendBufferSize", 128 * 1024);
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("backlog", 1024);

			bootstrap.setPipelineFactory(new BrokerSslPipelineFactory());

			InetSocketAddress inet0 = new InetSocketAddress("0.0.0.0", _portNumber);
			bootstrap.bind(inet0);

			log.info("SAPO-SSL-BROKER  Listening on: '{}'.", inet0.toString());
		}
		catch (Throwable t)
		{
			log.error("SAPO-SSL-BROKER failed to start. Reason: '{}'. The SSL endoint is not available", t.getMessage());
		}
	}
}