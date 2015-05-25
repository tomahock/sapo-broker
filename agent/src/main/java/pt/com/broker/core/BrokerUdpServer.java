package pt.com.broker.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;

import java.net.InetSocketAddress;

import org.caudexorigo.Shutdown;
import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.NoFramingDecoder;
import pt.com.broker.codec.NoFramingEncoder;
import pt.com.broker.codec.UdpFramingDecoder;
import pt.com.broker.net.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.gcs.conf.GcsInfo;

public class BrokerUdpServer
{
	private static Logger log = LoggerFactory.getLogger(BrokerUdpServer.class);

	private static final int MAX_UDP_MESSAGE_SIZE = 65 * 1024;


    private final InetSocketAddress socketAddress;
    private final InetSocketAddress legacySocketAddress;

    private final NoFramingEncoder noFramingEncoder = new NoFramingEncoder();
    private final AuthorizationFilter authorizationFilter =  new AuthorizationFilter();
    private final UdpFramingDecoder udpFramingDecoder =  new UdpFramingDecoder(GcsInfo.getMessageMaxSize());

	private NettyContext nettyCtx;

	public BrokerUdpServer(int legacyPort, int binProtoPort, NettyContext nettyCtx)
	{
		super();
		this.nettyCtx = nettyCtx;
		this.socketAddress = new InetSocketAddress("0.0.0.0", binProtoPort);
		this.legacySocketAddress = new InetSocketAddress("0.0.0.0", legacyPort);
	}

	public void start()
	{
		try
		{
			ChannelFuture future = startUdpServer();

            future.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {

                    if(channelFuture.isSuccess()){
                        log.info("SAPO-UDP-BROKER BINARY Listening on: '{}'.", channelFuture.channel().localAddress());
                    }
                }
            });
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}

		try
		{

            ChannelFuture future = startUdpLegacyServer();

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {

                    if(channelFuture.isSuccess()){
                        log.info("SAPO-UDP-BROKER LEGACY Listening on: '{}'.", channelFuture.channel().localAddress());
                    }
                }
            });
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}
	}


    private final ChannelFuture startUdpLegacyServer(){

        // Legacy message format

        Bootstrap legacyBootstrap = createBootstrap();


        legacyBootstrap.handler(new ChannelInitializer<DatagramChannel>() {

            @Override
            protected void initChannel(DatagramChannel datagramChannel) throws Exception {

                super.initChannel(datagramChannel);

                ChannelPipeline pipeline = datagramChannel.pipeline();

                pipeline.addAfter("broker-encoder", "broker-decoder", new NoFramingDecoder());

            }
        });

        return legacyBootstrap.bind(legacySocketAddress);

    }


    private final ChannelFuture startUdpServer(){

        Bootstrap bootstrap = createBootstrap();

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {

            @Override
            protected void initChannel(DatagramChannel datagramChannel) throws Exception {

                super.initChannel(datagramChannel);

                ChannelPipeline pipeline = datagramChannel.pipeline();

                pipeline.addAfter("broker-encoder","broker-decoder", udpFramingDecoder);

            }
        });

        return bootstrap.bind(socketAddress);

    }


    private final Bootstrap createBootstrap(){

        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup bossGroup = nettyCtx.getBossEventLoopGroup();

        bootstrap.group(bossGroup);

        bootstrap.channel(nettyCtx.getDatagramChannelClass());
        bootstrap.option(ChannelOption.SO_BROADCAST, true);


        /* @todo ver udp package size*/
        //bootstrap1.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(MAX_UDP_MESSAGE_SIZE));


        return bootstrap;
    }

    private class ChannelInitializer<T extends Channel> extends io.netty.channel.ChannelInitializer<T>{

        @Override
        protected void initChannel(T ch) throws Exception {

            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("broker-encoder", noFramingEncoder);

            if (GcsInfo.useAccessControl())
            {
                pipeline.addLast("broker-auth-filter", authorizationFilter);
            }

            pipeline.addLast("broker-handler", BrokerProtocolHandler.getInstance());


        }
    }

}