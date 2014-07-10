package pt.com.broker.core;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.broker.net.AuthorizationFilter;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.net.BrokerSslPipelineFactory;
import pt.com.gcs.conf.GcsInfo;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * BrokerServer is responsible for initializing client's SSL interface (MINA infrastructure).
 */

public class BrokerSSLServer extends BrokerServer
{

	private static Logger log = LoggerFactory.getLogger(BrokerSSLServer.class);


    private static BrokerSslPipelineFactory sslPipelineFactory  = new BrokerSslPipelineFactory();


	public BrokerSSLServer(ThreadFactory tf_io, ThreadFactory tf_workers, int portNumber)
	{
        super(tf_io,tf_workers,portNumber,0);
	}

	public void start()
	{

		try
		{

            ChannelFuture future = startSSLBrokerServer();


            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {

                    if(channelFuture.isSuccess()){
                        log.info("SAPO-SSL-BROKER  Listening on: '{}'.", channelFuture.channel().localAddress());
                    }
                }
            });


		}
		catch (Throwable t)
		{
			log.error("SAPO-SSL-BROKER failed to start. Reason: '{}'. The SSL endoint is not available", t.getMessage());
		}
	}

    private final SSLEngine getSSLEngine() throws Exception {

        SSLContext sslContext = sslPipelineFactory.getSSLContext();

        SSLEngine sslEngine = sslContext.createSSLEngine();

        sslEngine.setUseClientMode(false);

        return sslEngine;

    }

    protected ChannelFuture startSSLBrokerServer(){


        ServerBootstrap bootstrap = createBootstrap();


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>(){

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {

                // Create a default pipeline implementation.
                ChannelPipeline pipeline = ch.pipeline();

                SSLEngine sslEngine = getSSLEngine();

                SslHandler sslHandler = new SslHandler(sslEngine);

                pipeline.addLast("ssl", sslHandler);

                pipeline.addLast("broker-encoder", new BrokerEncoderRouter());
                pipeline.addLast("broker-decoder", new BrokerDecoderRouter(GcsInfo.getMessageMaxSize()));

                super.initChannel(ch);
            }
        });


        return bootstrap.bind(socketAddress);


     }


    @Override
    protected ServerBootstrap createBootstrap() {

        ServerBootstrap bootstrap =  super.createBootstrap();

        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        return bootstrap;
    }
}