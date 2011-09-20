package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

import org.caudexorigo.Shutdown;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.broker.codec.xml.SoapDecoder;
import pt.com.broker.codec.xml.SoapEncoder;
import pt.com.broker.net.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.SimpleFramingDecoder;
import pt.com.broker.types.SimpleFramingEncoder;
import pt.com.gcs.conf.GcsInfo;

/**
 * BrokerServer is responsible for initializing client's TCP interface (Netty infrastructure).
 */

public class BrokerServer
{
	private static Logger log = LoggerFactory.getLogger(BrokerServer.class);

	private int _portNumber;

	private int _legacyPortNumber;

	public static final int WRITE_BUFFER_HIGH_WATER_MARK = 128 * 1024;

	private final ThreadPoolExecutor tpeIo;
	private final ThreadPoolExecutor tpeWorkers;

	public BrokerServer(ThreadPoolExecutor tpe_io, ThreadPoolExecutor tpe_workers, int portNumber, int legacyPortNumber)
	{
		tpeIo = tpe_io;
		tpeWorkers = tpe_workers;
		_portNumber = portNumber;
		_legacyPortNumber = legacyPortNumber;
	}

	public void start()
	{
		try
		{

			ChannelFactory factory0 = new NioServerSocketChannelFactory(tpeIo, tpeWorkers);
			ServerBootstrap bootstrap0 = new ServerBootstrap(factory0);

			bootstrap0.setOption("child.tcpNoDelay", false);
			bootstrap0.setOption("child.keepAlive", true);
			bootstrap0.setOption("child.receiveBufferSize", 256 * 1024);
			bootstrap0.setOption("child.sendBufferSize", 256 * 1024);
			bootstrap0.setOption("child.soLinger", 1);
			bootstrap0.setOption("reuseAddress", true);
			bootstrap0.setOption("backlog", 1024);
			bootstrap0.setOption("writeBufferHighWaterMark", WRITE_BUFFER_HIGH_WATER_MARK); // default=64K
			// bootstrap0.setOption("writeBufferLowWaterMark", 1024); // default=32K

			// water marks introduction:
			// http://www.jboss.org/netty/community.html#nabble-td1611593

			ChannelPipelineFactory serverPipelineFactory0 = new ChannelPipelineFactory()
			{
				@Override
				public ChannelPipeline getPipeline() throws Exception
				{
					ChannelPipeline pipeline = Channels.pipeline();

					pipeline.addLast("broker-legacy-framing-decoder", new SimpleFramingDecoder());
					pipeline.addLast("broker-legacy-xml-decoder", new SoapDecoder());

					if (GcsInfo.useAccessControl())
					{
						pipeline.addLast("broker-auth-filter", new AuthorizationFilter());
					}

					pipeline.addLast("broker-legacy-framing-encoder", new SimpleFramingEncoder());
					pipeline.addLast("broker-legacy-xml-encoder", new SoapEncoder());

					pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());

					return pipeline;
				}
			};

			bootstrap0.setPipelineFactory(serverPipelineFactory0);

			InetSocketAddress inet0 = new InetSocketAddress("0.0.0.0", _legacyPortNumber);
			bootstrap0.bind(inet0);
			log.info("SAPO-BROKER (legacy protocol)  Listening on: '{}'.", inet0.toString());

			ChannelFactory factory1 = new NioServerSocketChannelFactory(tpeIo, tpeWorkers);
			ServerBootstrap bootstrap1 = new ServerBootstrap(factory1);

			ChannelPipelineFactory serverPipelineFactory1 = new ChannelPipelineFactory()
			{
				@Override
				public ChannelPipeline getPipeline() throws Exception
				{
					ChannelPipeline pipeline = Channels.pipeline();

					pipeline.addLast("broker-encoder", new BrokerEncoderRouter());

					pipeline.addLast("broker-decoder", new BrokerDecoderRouter(GcsInfo.getMessageMaxSize()));

					if (GcsInfo.useAccessControl())
					{
						pipeline.addLast("broker-auth-filter", new AuthorizationFilter());
					}

					pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());

					return pipeline;
				}
			};

			bootstrap1.setPipelineFactory(serverPipelineFactory1);

			bootstrap1.setOption("child.tcpNoDelay", true);
			bootstrap1.setOption("child.keepAlive", true);
			bootstrap1.setOption("child.receiveBufferSize", 128 * 1024);
			bootstrap1.setOption("child.sendBufferSize", 128 * 1024);
			bootstrap1.setOption("child.soLinger", 1);
			bootstrap1.setOption("reuseAddress", true);
			bootstrap1.setOption("backlog", 1024);
			bootstrap1.setOption("writeBufferHighWaterMark", WRITE_BUFFER_HIGH_WATER_MARK); // default=64K
			// bootstrap1.setOption("writeBufferLowWaterMark", 1024); // default=32K

			InetSocketAddress inet1 = new InetSocketAddress("0.0.0.0", _portNumber);
			bootstrap1.bind(inet1);
			log.info("SAPO-BROKER Listening on: '{}'.", inet1.toString());

		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}
	}
}
