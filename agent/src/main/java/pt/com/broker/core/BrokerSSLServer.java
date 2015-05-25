package pt.com.broker.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.BrokerDecoderRouter;
import pt.com.broker.codec.BrokerEncoderRouter;
import pt.com.broker.net.BrokerSslPipelineFactory;
import pt.com.gcs.conf.GcsInfo;

/**
 * BrokerServer is responsible for initializing client's SSL interface (MINA infrastructure).
 */

public class BrokerSSLServer extends BrokerServer
{

	private static Logger log = LoggerFactory.getLogger(BrokerSSLServer.class);


    private static BrokerSslPipelineFactory sslPipelineFactory  = new BrokerSslPipelineFactory();


	public BrokerSSLServer(int portNumber, NettyContext nettyCtx)
	{
        super(portNumber,0, nettyCtx);
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
        
        String sslWhiteListProtocolsStr = GcsInfo.getSslProtocolWhiteList();
        //Validate and apply Ssl White List Protocols.
        if(sslWhiteListProtocolsStr != null){
        	String[] sslSupportedProtocols = sslEngine.getSupportedProtocols();
        	String[] sslWhiteListProtocols = sslWhiteListProtocolsStr.replaceAll("\\s*,\\s*", ",").split(",");
        	List<String> validSslWhiteListProtocols = new ArrayList<String>();
        	for(String sslWhiteListProtocol: sslWhiteListProtocols){
        		if(Arrays.binarySearch(sslSupportedProtocols, sslWhiteListProtocol) > 0){
        			//Valid protocol string.
        			validSslWhiteListProtocols.add(sslWhiteListProtocol);
        		} else {
        			log.warn("Invalid SSL protocol configuration found: {}", sslWhiteListProtocol);
        		}
        	}
        	if(validSslWhiteListProtocols.size() > 0){
        		sslEngine.setEnabledProtocols(validSslWhiteListProtocols.toArray(new String[validSslWhiteListProtocols.size()]));
        	}
        }
        
        String sslWhiteListCipherSuiteStr = GcsInfo.getSslCipherSuiteWhitelist();
        //Validate and apply Ssl CipherSuites white lists
        if(sslWhiteListCipherSuiteStr != null){
        	String[] sslSupportedCipherSuites = sslEngine.getSupportedCipherSuites();
        	String[] sslWhiteListCipherSuites = sslWhiteListCipherSuiteStr.replaceAll("\\s*,\\s*", ",").split(",");
        	
        	List<String> validSslWhiteListCipherSuites = new ArrayList<String>();
        	for(String sslWhiteListCipherSuite: sslWhiteListCipherSuites){
        		if(Arrays.binarySearch(sslSupportedCipherSuites, sslWhiteListCipherSuite) > 0){
        			//Valid protocol string.
        			validSslWhiteListCipherSuites.add(sslWhiteListCipherSuite);
        		} else {
        			log.warn("Invalid SSL ciphersuite configuration found: {}", sslWhiteListCipherSuite);
        		}
        	}
        	if(validSslWhiteListCipherSuites.size() > 0){
        		sslEngine.setEnabledCipherSuites(validSslWhiteListCipherSuites.toArray(new String[validSslWhiteListCipherSuites.size()]));
        	}
        	
        }
        
        sslEngine.setUseClientMode(false);
        
        log.debug("SSLEngine enabled protocols: {}", Arrays.toString(sslEngine.getEnabledProtocols()));
        log.debug("SSLEngine supported protocols: {}", Arrays.toString(sslEngine.getSupportedProtocols()));
        log.debug("SSLEngine supported ciphersuites: {}", Arrays.toString(sslEngine.getSupportedCipherSuites()));

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