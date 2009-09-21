package pt.com.broker.jsbridge.bayeux;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.jsbridge.CommunicationManager;
import pt.com.broker.jsbridge.configuration.ConfigurationInfo;

public class BayeuxServer
{
	private static final Logger log = LoggerFactory.getLogger(BayeuxServer.class);
	
	public static void main(String[] args)
	{
		System.out.println("Starting...");

		// Configuration initialization
		ConfigurationInfo.init();
		
		if( ConfigurationInfo.getConfiguration() == null)
		{
			log.error("Configuration failed!");
			System.out.println("Exiting...");
			return;
		}
		
		// Subscription initialization
		if( ! CommunicationManager.init() )
		{
			log.error("CommunicationManager initialization failed.");
			System.out.println("Exiting...");
			return;
		}
				
		// Communication initialization
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new BayeuxServerPipelineFactory());
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());


		int port = ConfigurationInfo.getConfiguration().getSettings().getBridge().getPort().intValue();
		
		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));
	}
}
