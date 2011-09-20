package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.caudexorigo.Shutdown;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.broker.codec.NoFramingDecoder;
import pt.com.broker.codec.NoFramingEncoder;
import pt.com.broker.net.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.gcs.conf.GcsInfo;

public class BrokerUdpServer
{
	private static Logger log = LoggerFactory.getLogger(BrokerUdpServer.class);

	private static final int MAX_UDP_MESSAGE_SIZE = 65 * 1024;

	private int _legacyPort;
	private int _binProtoPort;
	private final Executor tpeIo;

	public BrokerUdpServer(Executor tpe_io, int legacyPort, int binProtoPort)
	{
		super();
		tpeIo = tpe_io;
		_legacyPort = legacyPort;
		_binProtoPort = binProtoPort;
	}

	public void start()
	{
		try
		{
			// Legacy message format

			DatagramChannelFactory datagramChannelFactory0 = new NioDatagramChannelFactory(tpeIo);

			ConnectionlessBootstrap bootstrap0 = new ConnectionlessBootstrap(datagramChannelFactory0);

			ChannelPipelineFactory serverPipelineFactory0 = new ChannelPipelineFactory()
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

			bootstrap0.setPipelineFactory(serverPipelineFactory0);
			// bootstrap0.setOption("broadcast", "false");
			bootstrap0.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(MAX_UDP_MESSAGE_SIZE));

			InetSocketAddress inet0 = new InetSocketAddress("0.0.0.0", _binProtoPort);
			bootstrap0.bind(inet0);
			log.info("SAPO-UDP-BROKER BINARY Listening on: '{}'.", inet0.toString());
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}

		try
		{
			// Legacy message format

			DatagramChannelFactory datagramChannelFactory1 = new NioDatagramChannelFactory(tpeIo);

			ConnectionlessBootstrap bootstrap1 = new ConnectionlessBootstrap(datagramChannelFactory1);

			ChannelPipelineFactory serverPipelineFactory1 = new ChannelPipelineFactory()
			{
				@Override
				public ChannelPipeline getPipeline() throws Exception
				{
					ChannelPipeline pipeline = Channels.pipeline();

					pipeline.addLast("broker-encoder", new NoFramingEncoder());

					pipeline.addLast("broker-decoder", new NoFramingDecoder());

					if (GcsInfo.useAccessControl())
					{
						pipeline.addLast("broker-auth-filter", new AuthorizationFilter());
					}

					pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());

					return pipeline;
				}
			};

			bootstrap1.setPipelineFactory(serverPipelineFactory1);
			// bootstrap1.setOption("broadcast", "false");
			bootstrap1.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(MAX_UDP_MESSAGE_SIZE));

			InetSocketAddress inet1 = new InetSocketAddress("0.0.0.0", _legacyPort);
			bootstrap1.bind(inet1);
			log.info("SAPO-UDP-BROKER LEGACY Listening on: '{}'.", inet1.toString());
		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);
			Shutdown.now();
		}
	}
}