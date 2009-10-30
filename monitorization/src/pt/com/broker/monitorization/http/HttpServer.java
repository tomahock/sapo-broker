package pt.com.broker.monitorization.http;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collectors.CollectorManager;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;
import pt.com.broker.monitorization.consolidator.db.H2ConsolidatorManager;

public class HttpServer
{
	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
	
	public static void main(String[] args)
	{
		System.out.println("Starting Sapo-Broker HTTP Monitorization Server...");

		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

		// Configuration initialization
		// Communication initialization
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new MonitorizationPipelineFactory());
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		
		
		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(8877));
		
		
		ConfigurationInfo.init();
		
		CollectorManager.init();
		
		//ConsolidatorManager.init();
		H2ConsolidatorManager.init();
	}
}
