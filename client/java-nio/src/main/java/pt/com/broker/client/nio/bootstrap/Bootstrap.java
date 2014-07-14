package pt.com.broker.client.nio.bootstrap;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import pt.com.broker.client.nio.NioSocketChannelBroker;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 23-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class Bootstrap extends BaseBootstrap {


    /**
     * <p>Constructor for Bootstrap.</p>
     *
     * @param channelInitializer a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
     */
    public Bootstrap(BaseChannelInitializer channelInitializer) {
        super(channelInitializer);
    }

    /**
     * <p>getNewInstance.</p>
     *
     * @return a {@link io.netty.bootstrap.Bootstrap} object.
     */
    public io.netty.bootstrap.Bootstrap getNewInstance(){

        io.netty.bootstrap.Bootstrap bootstrap = new io.netty.bootstrap.Bootstrap();

        EventLoopGroup group = getGroup();


       bootstrap.group(group).channel(NioSocketChannelBroker.class);


       bootstrap.handler(getChannelInitializer());

        return  bootstrap;
    }


    @Override
    public ChannelFuture connect(final HostInfo hostInfo) {
        ChannelFuture f = super.connect(hostInfo);


        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {

                if (f.isSuccess()) {

                    IdleStateHandler idleStateHandler = new IdleStateHandler(hostInfo.getReaderIdleTime(), hostInfo.getWriterIdleTime(), 0, TimeUnit.MILLISECONDS);

                    f.channel().pipeline().addBefore("heartbeat_handler", "idle_state_handler", idleStateHandler);


                }
            }
        });


        return f;
    }
}
