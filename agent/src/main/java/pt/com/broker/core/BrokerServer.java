package pt.com.broker.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;

import org.caudexorigo.Shutdown;
import org.caudexorigo.netty.NettyContext;
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

	final InetSocketAddress socketAddress;
	final InetSocketAddress legacySocketAddress;

	public static final int WRITE_BUFFER_HIGH_WATER_MARK = 128 * 1024;

	private final SoapDecoder soapDecoder = new SoapDecoder();
	private final SoapEncoder soapEncoder = new SoapEncoder();
	protected final AuthorizationFilter authorizationFilter = new AuthorizationFilter();
	protected final BrokerEncoderRouter brokerEncoderRouter = new BrokerEncoderRouter();

	private final NettyContext nettyCtx;

	public BrokerServer(int portNumber, int legacyPortNumber, NettyContext nettyCtx)
	{
		this.nettyCtx = nettyCtx;
		this.socketAddress = new InetSocketAddress("0.0.0.0", portNumber);
		this.legacySocketAddress = new InetSocketAddress("0.0.0.0", legacyPortNumber);
	}

	public void start()
	{
		try
		{

			ChannelFuture futureLegacy = startLegacyTcpServer();

			futureLegacy.addListener(new ChannelFutureListener()
			{

				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception
				{
					log.info("SAPO-BROKER (legacy protocol)  Listening on: '{}'.", channelFuture.channel().localAddress());
				}

			});

			ChannelFuture future = startTcpServer();

			future.addListener(new ChannelFutureListener()
			{
				@Override
				public void operationComplete(ChannelFuture channelFuture) throws Exception
				{
					log.info("SAPO-BROKER Listening on: '{}'.", channelFuture.channel().localAddress());
				}
			});

		}
		catch (Throwable e)
		{
			log.error(e.getMessage(), e);

			Shutdown.now();
		}
	}

	protected ChannelFuture startLegacyTcpServer()
	{

		ServerBootstrap bootstrap = createBootstrap();

		bootstrap.childHandler(new ChannelInitializer<SocketChannel>()
		{

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception
			{

				ChannelPipeline pipeline = socketChannel.pipeline();

				pipeline.addLast("broker-legacy-framing-decoder", new SimpleFramingDecoder());
				pipeline.addLast("broker-legacy-xml-decoder", soapDecoder);

				super.initChannel(socketChannel);

				pipeline.addBefore("broker-handler", "broker-legacy-framing-encoder", new SimpleFramingEncoder());
				pipeline.addBefore("broker-handler", "broker-legacy-xml-encoder", soapEncoder);

			}
		});

		return bootstrap.bind(legacySocketAddress);

	}

	protected ChannelFuture startTcpServer()
	{

		ServerBootstrap bootstrap = createBootstrap();

		bootstrap.childHandler(new ChannelInitializer<SocketChannel>()
		{

			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception
			{

				ChannelPipeline pipeline = socketChannel.pipeline();

				pipeline.addLast("broker-encoder", brokerEncoderRouter);

				pipeline.addLast("broker-decoder", new BrokerDecoderRouter(GcsInfo.getMessageMaxSize()));

				super.initChannel(socketChannel);

			}
		});

		return bootstrap.bind(socketAddress);

	}

	protected ServerBootstrap createBootstrap()
	{

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.channel(nettyCtx.getServerChannelClass());

		bootstrap.group(nettyCtx.getBossEventLoopGroup(), nettyCtx.getWorkerEventLoopGroup());

		bootstrap.childOption(ChannelOption.ALLOCATOR, nettyCtx.getAllocator());
		bootstrap.childOption(ChannelOption.TCP_NODELAY, false);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.SO_RCVBUF, 256 * 1024);
		bootstrap.childOption(ChannelOption.SO_SNDBUF, 256 * 1024);
		bootstrap.childOption(ChannelOption.SO_LINGER, -1);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, WRITE_BUFFER_HIGH_WATER_MARK);// default=64K

		// bootstrap0.setOption("writeBufferLowWaterMark", 1024); // default=32K

		// water marks introduction:
		// http://www.jboss.org/netty/community.html#nabble-td1611593

		return bootstrap;
	}

	public int getPortNumber()
	{
		return socketAddress.getPort();
	}

	protected class ChannelInitializer<T extends Channel> extends io.netty.channel.ChannelInitializer<T>
	{

		@Override
		protected void initChannel(T ch) throws Exception
		{

			ChannelPipeline pipeline = ch.pipeline();

			if (GcsInfo.useAccessControl())
			{
				pipeline.addLast("broker-auth-filter", authorizationFilter);
			}

			pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());

		}
	}

}
