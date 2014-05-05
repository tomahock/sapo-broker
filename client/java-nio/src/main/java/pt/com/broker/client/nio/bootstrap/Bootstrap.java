package pt.com.broker.client.nio.bootstrap;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.NioSocketChannelBroker;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by luissantos on 23-04-2014.
 */
public class Bootstrap extends BaseBootstrap {



    private NetProtocolType protocolType;

    ConsumerManager consumerManager;

    PongConsumerManager pongConsumerManager;


    public Bootstrap(NetProtocolType protocolType , ConsumerManager consumerManager, PongConsumerManager pongConsumerManager, boolean oldFraming) {

        setProtocolType(protocolType);

        setBootstrap(new io.netty.bootstrap.Bootstrap());

        setPongConsumerManager(pongConsumerManager);
        setConsumerManager(consumerManager);


        init();
    }

    public void init(){


        EventLoopGroup group = new NioEventLoopGroup();


        getBootstrap().group(group).channel(NioSocketChannelBroker.class);


        ChannelInitializer channelInitializer = new ChannelInitializer(getProtocolType(),getConsumerManager(),getPongConsumerManager());

        channelInitializer.setContext(getDefaultSslContext());


        getBootstrap().handler(channelInitializer);

    }

    private static SSLContext getDefaultSslContext()
    {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public X509Certificate[] getAcceptedIssuers()
            {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
            {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
            {

            }
        } };
        try
        {
            SSLContext sc = SSLContext.getInstance("SSLv3");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            return sc;
        }
        catch (Throwable t)
        {
            throw new RuntimeException(t);
        }
    }



    public io.netty.bootstrap.Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(io.netty.bootstrap.Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public NetProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(NetProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    public PongConsumerManager getPongConsumerManager() {
        return pongConsumerManager;
    }

    public void setPongConsumerManager(PongConsumerManager pongConsumerManager) {
        this.pongConsumerManager = pongConsumerManager;
    }


}
